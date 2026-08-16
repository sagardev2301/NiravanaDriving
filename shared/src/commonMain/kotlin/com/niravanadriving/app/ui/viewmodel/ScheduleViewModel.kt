package com.niravanadriving.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.niravanadriving.app.data.models.Lesson
import com.niravanadriving.app.data.repository.InstructorRepository
import com.niravanadriving.app.data.repository.LessonRepository
import com.niravanadriving.app.ui.util.UiState
import com.niravanadriving.app.data.supabase
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.RealtimeChannel
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.realtime
import io.github.jan.supabase.postgrest.query.filter.FilterOperator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

class ScheduleViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<UiState<Unit>>(UiState.Loading)
    val uiState: StateFlow<UiState<Unit>> = _uiState.asStateFlow()

    private val _lessons = MutableStateFlow<List<Lesson>>(emptyList())
    val lessons: StateFlow<List<Lesson>> = _lessons.asStateFlow()

    private var lessonsChannel: RealtimeChannel? = null

    val tomorrow = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date.plus(1, DateTimeUnit.DAY)

    init {
        loadData(isRefresh = false)
    }

    fun refresh() {
        loadData(isRefresh = true)
    }

    private fun loadData(isRefresh: Boolean) {
        if (!isRefresh && _uiState.value is UiState.Success) return

        viewModelScope.launch {
            if (!isRefresh) _uiState.value = UiState.Loading
            try {
                val instructor = InstructorRepository.getCurrentInstructor()
                if (instructor == null) {
                    _uiState.value = UiState.Error("Instructor not found")
                    return@launch
                }
                _lessons.value = LessonRepository.getLessonsForDate(instructor.id, tomorrow)
                _uiState.value = UiState.Success(Unit)
                
                subscribeToRealtime(instructor.id)
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Error loading schedule")
            }
        }
    }

    private fun subscribeToRealtime(instructorId: String) {
        viewModelScope.launch {
            lessonsChannel?.unsubscribe()
            lessonsChannel = supabase.realtime.channel("schedule-changes-$instructorId")
            
            lessonsChannel?.postgresChangeFlow<PostgresAction>(schema = "public") {
                table = "lessons"
                filter(column = "instructor_id", operator = FilterOperator.EQ, value = instructorId)
            }?.onEach { 
                loadData(isRefresh = true)
            }?.launchIn(viewModelScope)

            lessonsChannel?.subscribe()
        }
    }

    fun autoFillFromYesterday(onNoLessons: () -> Unit, onSuccess: () -> Unit) {
        viewModelScope.launch {
            val instructor = InstructorRepository.getCurrentInstructor()
            if (instructor != null) {
                val yesterdayLessons = LessonRepository.getYesterdayPublishedLessons(instructor.id)
                if (yesterdayLessons.isEmpty()) {
                    onNoLessons()
                } else {
                    val newLessons = yesterdayLessons.map {
                        it.copy(
                            id = null,
                            scheduledDate = tomorrow.toString(),
                            isDraft = true
                        )
                    }
                    _lessons.value = _lessons.value + newLessons
                    onSuccess()
                }
            }
        }
    }

    fun saveDraft(onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = LessonRepository.saveDraftLessons(_lessons.value)
            if (success) loadData(isRefresh = true)
            onResult(success)
        }
    }

    fun publish(onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val instructor = InstructorRepository.getCurrentInstructor()
            if (instructor != null) {
                if (LessonRepository.saveDraftLessons(_lessons.value)) {
                    val success = LessonRepository.publishLessonsForDate(instructor.id, tomorrow)
                    if (success) {
                        // TODO: trigger notification
                        loadData(isRefresh = true)
                    }
                    onResult(success)
                } else {
                    onResult(false)
                }
            } else {
                onResult(false)
            }
        }
    }

    fun removeLesson(index: Int, lesson: Lesson, onError: (String) -> Unit) {
        viewModelScope.launch {
            val id = lesson.id
            if (id != null) {
                if (LessonRepository.deleteLesson(id)) {
                    _lessons.value = _lessons.value.filter { it.id != id }
                } else {
                    onError("Failed to delete lesson")
                }
            } else {
                _lessons.value = _lessons.value.filterIndexed { i, _ -> i != index }
            }
        }
    }

    override fun onCleared() {
        lessonsChannel?.let { 
            viewModelScope.launch { it.unsubscribe() }
        }
    }
}
