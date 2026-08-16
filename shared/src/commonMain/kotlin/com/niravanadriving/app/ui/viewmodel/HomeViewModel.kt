package com.niravanadriving.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.niravanadriving.app.data.models.Instructor
import com.niravanadriving.app.data.models.Lesson
import com.niravanadriving.app.data.models.LessonSession
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

data class HomeUiData(
    val instructor: Instructor,
    val ongoingLesson: Pair<Lesson, LessonSession>?,
    val todaySchedule: List<Lesson>
)

class HomeViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<UiState<HomeUiData>>(UiState.Loading)
    val uiState: StateFlow<UiState<HomeUiData>> = _uiState.asStateFlow()

    private var lessonsChannel: RealtimeChannel? = null
    private var sessionsChannel: RealtimeChannel? = null

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
                    _uiState.value = UiState.Error("Failed to resolve instructor profile")
                    return@launch
                }
                val ongoing = LessonRepository.getOngoingLesson(instructor.id)
                val schedule = LessonRepository.getTodayLessons(instructor.id)
                _uiState.value = UiState.Success(HomeUiData(instructor, ongoing, schedule))
                
                subscribeToRealtime(instructor.id)
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Unknown error occurred")
            }
        }
    }

    private fun subscribeToRealtime(instructorId: String) {
        viewModelScope.launch {
            lessonsChannel?.unsubscribe()
            lessonsChannel = supabase.realtime.channel("lessons-changes-$instructorId")
            
            lessonsChannel?.postgresChangeFlow<PostgresAction>(schema = "public") {
                table = "lessons"
                filter(column = "instructor_id", operator = FilterOperator.EQ, value = instructorId)
            }?.onEach { action ->
                // Realtime actions usually don't include joined data.
                // For KMP lessons which have student/vehicle, a refresh is safest 
                // to maintain consistent UI state with joins.
                loadData(isRefresh = true)
            }?.launchIn(viewModelScope)

            sessionsChannel?.unsubscribe()
            sessionsChannel = supabase.realtime.channel("sessions-changes-$instructorId")
            sessionsChannel?.postgresChangeFlow<PostgresAction>(schema = "public") {
                table = "lesson_sessions"
            }?.onEach { 
                loadData(isRefresh = true)
            }?.launchIn(viewModelScope)

            lessonsChannel?.subscribe()
            sessionsChannel?.subscribe()
        }
    }

    @OptIn(kotlinx.coroutines.DelicateCoroutinesApi::class)
    override fun onCleared() {
        lessonsChannel?.let {
            kotlinx.coroutines.GlobalScope.launch { it.unsubscribe() }
        }
        sessionsChannel?.let {
            kotlinx.coroutines.GlobalScope.launch { it.unsubscribe() }
        }
    }
}
