package com.niravanadriving.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.niravanadriving.app.data.models.Student
import com.niravanadriving.app.data.repository.InstructorRepository
import com.niravanadriving.app.data.repository.StudentRepository
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

class LearnerViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<UiState<List<Student>>>(UiState.Loading)
    val uiState: StateFlow<UiState<List<Student>>> = _uiState.asStateFlow()

    private var studentsChannel: RealtimeChannel? = null
    private var paymentsChannel: RealtimeChannel? = null

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
                val students = StudentRepository.getAllStudents(instructor.id)
                _uiState.value = UiState.Success(students)
                
                subscribeToRealtime(instructor.id)
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Error loading learners")
            }
        }
    }

    private fun subscribeToRealtime(instructorId: String) {
        viewModelScope.launch {
            studentsChannel?.unsubscribe()
            studentsChannel = supabase.realtime.channel("students-changes-$instructorId")
            
            studentsChannel?.postgresChangeFlow<PostgresAction>(schema = "public") {
                table = "students"
                filter(column = "instructor_id", operator = FilterOperator.EQ, value = instructorId)
            }?.onEach { 
                loadData(isRefresh = true)
            }?.launchIn(viewModelScope)

            paymentsChannel?.unsubscribe()
            paymentsChannel = supabase.realtime.channel("payments-changes-$instructorId")
            paymentsChannel?.postgresChangeFlow<PostgresAction>(schema = "public") {
                table = "payments"
                filter(column = "instructor_id", operator = FilterOperator.EQ, value = instructorId)
            }?.onEach { 
                loadData(isRefresh = true)
            }?.launchIn(viewModelScope)

            studentsChannel?.subscribe()
            paymentsChannel?.subscribe()
        }
    }

    override fun onCleared() {
        studentsChannel?.let { 
            viewModelScope.launch { it.unsubscribe() }
        }
        paymentsChannel?.let {
            viewModelScope.launch { it.unsubscribe() }
        }
    }
}
