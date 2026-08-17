package com.niravanadriving.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.niravanadriving.app.data.models.Lesson
import com.niravanadriving.app.data.repository.InstructorRepository
import com.niravanadriving.app.data.repository.LessonRepository
import com.niravanadriving.app.ui.util.UiState
import com.niravanadriving.app.data.supabase
import io.github.jan.supabase.auth.auth
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
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

class ScheduleViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<UiState<Unit>>(UiState.Loading)
    val uiState: StateFlow<UiState<Unit>> = _uiState.asStateFlow()

    private val _lessons = MutableStateFlow<List<Lesson>>(emptyList())
    val lessons: StateFlow<List<Lesson>> = _lessons.asStateFlow()

    private val _selectedDate = MutableStateFlow(
        Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
            .plus(1, DateTimeUnit.DAY)
    )
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    private var lessonsChannel: RealtimeChannel? = null

    init {
        // Observe session status and trigger fetch when logged in
        supabase.auth.sessionStatus
            .onEach { status ->
                if (status is io.github.jan.supabase.auth.status.SessionStatus.Authenticated) {
                    loadData(isRefresh = false)
                }
            }
            .launchIn(viewModelScope)
    }

    fun selectDate(date: LocalDate) {
        if (_selectedDate.value == date) return
        _selectedDate.value = date
        loadData(isRefresh = false)
    }

    fun refresh() {
        loadData(isRefresh = true)
    }

    private fun loadData(isRefresh: Boolean) {
        // Force refresh if isRefresh is true OR if we aren't already in success state
        // When changing dates, selectDate calls loadData(false), but we still want to fetch.
        // So we reset to Loading if not a refresh.
        if (!isRefresh && _uiState.value is UiState.Success) {
            // If we are already in success and this isn't a silent background refresh,
            // we should still proceed because the date might have changed.
            // But selectDate resets the state to Loading if we want the spinner.
        }

        viewModelScope.launch {
            if (!isRefresh) _uiState.value = UiState.Loading
            try {
                val instructor = InstructorRepository.getCurrentInstructor()
                if (instructor == null) {
                    _uiState.value = UiState.Error("Instructor not found")
                    return@launch
                }
                _lessons.value = LessonRepository.getLessonsForDate(instructor.id, _selectedDate.value)
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

    fun autoFillFromPreviousDay(onNoLessons: () -> Unit, onSuccess: () -> Unit) {
        viewModelScope.launch {
            val instructor = InstructorRepository.getCurrentInstructor()
            if (instructor != null) {
                val previousDate = _selectedDate.value.minus(1, DateTimeUnit.DAY)
                val previousDayLessons = LessonRepository.getPublishedLessonsForDate(instructor.id, previousDate)
                if (previousDayLessons.isEmpty()) {
                    onNoLessons()
                } else {
                    val newLessons = previousDayLessons.map {
                        it.copy(
                            id = null,
                            scheduledDate = _selectedDate.value.toString(),
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
                    val success = LessonRepository.publishLessonsForDate(instructor.id, _selectedDate.value)
                    if (success) {
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

    fun addLesson(lesson: Lesson) {
        val newLesson = lesson.copy(
            id = null,
            isDraft = true,
            scheduledDate = _selectedDate.value.toString()
        )
        _lessons.value = _lessons.value + newLesson
    }

    fun updateLesson(lesson: Lesson, onResult: (Boolean) -> Unit) {
        val id = lesson.id
        if (id == null) {
            onResult(false)
            return
        }

        viewModelScope.launch {
            val success = LessonRepository.updateLesson(lesson)
            if (success) {
                _lessons.value = _lessons.value.map {
                    if (it.id == id) lesson else it
                }
            }
            onResult(success)
        }
    }

    fun getLessonById(id: String?): Lesson? =
        _lessons.value.find { it.id == id }

    @OptIn(kotlinx.coroutines.DelicateCoroutinesApi::class)
    override fun onCleared() {
        lessonsChannel?.let {
            kotlinx.coroutines.GlobalScope.launch { it.unsubscribe() }
        }
    }
}
