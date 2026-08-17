package com.niravanadriving.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.niravanadriving.app.data.models.Instructor
import com.niravanadriving.app.data.models.Payment
import com.niravanadriving.app.data.models.PaymentMethod
import com.niravanadriving.app.data.models.PaymentStatus
import com.niravanadriving.app.data.models.Student
import com.niravanadriving.app.data.supabase
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

data class AddLearnerFormState(
    val fullName: String = "",
    val fullNameError: String? = null,
    val phoneNumber: String = "",
    val phoneNumberError: String? = null,
    val emergencyContact: String = "",
    val totalClasses: Int? = 10,
    val totalClassesError: String? = null,
    val startDate: String = "", // Will be initialized in ViewModel init
    val pickupAddress: String = "",
    val totalFee: String = "",
    val advanceReceived: String = "",
    val isPaymentReceivedToday: Boolean = true,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSuccess: Boolean = false
)

class AddLearnerViewModel : ViewModel() {
    private val _state = MutableStateFlow(AddLearnerFormState())
    val state: StateFlow<AddLearnerFormState> = _state.asStateFlow()

    init {
        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        _state.value = _state.value.copy(startDate = formatDate(today))
    }

    private fun formatDate(date: kotlinx.datetime.LocalDate): String {
        val day = date.day.toString().padStart(2, '0')
        val month = date.monthNumber.toString().padStart(2, '0')
        val year = date.year
        return "$day-$month-$year"
    }

    fun onFullNameChange(value: String) {
        _state.value = _state.value.copy(fullName = value, fullNameError = null)
    }

    fun onPhoneNumberChange(value: String) {
        _state.value = _state.value.copy(phoneNumber = value, phoneNumberError = null)
    }

    fun onEmergencyContactChange(value: String) {
        _state.value = _state.value.copy(emergencyContact = value)
    }

    fun onTotalClassesChange(value: Int) {
        _state.value = _state.value.copy(totalClasses = value, totalClassesError = null)
    }

    fun onStartDateChange(value: kotlinx.datetime.LocalDate) {
        _state.value = _state.value.copy(startDate = formatDate(value))
    }

    fun onPickupAddressChange(value: String) {
        _state.value = _state.value.copy(pickupAddress = value)
    }

    fun onTotalFeeChange(value: String) {
        if (value.all { it.isDigit() }) {
            _state.value = _state.value.copy(totalFee = value)
        }
    }

    fun onAdvanceReceivedChange(value: String) {
        if (value.all { it.isDigit() }) {
            _state.value = _state.value.copy(advanceReceived = value)
        }
    }

    fun onPaymentReceivedTodayChange(value: Boolean) {
        _state.value = _state.value.copy(isPaymentReceivedToday = value)
    }

    fun saveLearner() {
        if (!validate()) return

        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, errorMessage = null)
            try {
                val currentUserId = supabase.auth.currentUserOrNull()?.id
                if (currentUserId == null) {
                    _state.value = _state.value.copy(isLoading = false, errorMessage = "User not authenticated")
                    return@launch
                }

                // Fetch Instructor
                val instructor = supabase.postgrest["instructors"]
                    .select {
                        filter {
                            eq("auth_user_id", currentUserId)
                        }
                    }
                    .decodeSingle<Instructor>()

                val student = Student(
                    instructorId = instructor.id,
                    fullName = _state.value.fullName,
                    phone = _state.value.phoneNumber,
                    address = _state.value.pickupAddress,
                    totalSessions = _state.value.totalClasses,
                    feePerSession = (_state.value.totalFee.toDoubleOrNull() ?: 0.0) / (_state.value.totalClasses?.toDouble() ?: 1.0),
                    instructorRemarks = "Emergency: ${_state.value.emergencyContact}",
                    isActive = true,
                    dateOfBirth = null // Schema matches?
                )

                val insertedStudent = supabase.postgrest["students"]
                    .insert(student) {
                        select()
                    }
                    .decodeSingle<Student>()

                val advance = _state.value.advanceReceived.toDoubleOrNull() ?: 0.0
                if (advance > 0 && insertedStudent.id != null) {
                    try {
                        val payment = Payment(
                            studentId = insertedStudent.id,
                            instructorId = instructor.id,
                            amount = advance,
                            paymentMethod = PaymentMethod.CASH,
                            status = PaymentStatus.PAID,
                            paidAt = if (_state.value.isPaymentReceivedToday) Clock.System.now().toString() else null,
                            notes = "Initial Advance"
                        )
                        supabase.postgrest["payments"].insert(payment)
                    } catch (e: Exception) {
                        // Log but don't fail
                    }
                }

                _state.value = _state.value.copy(isLoading = false, isSuccess = true)
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false, errorMessage = "Error: ${e.message}")
            }
        }
    }

    private fun validate(): Boolean {
        var isValid = true
        val currentState = _state.value

        if (currentState.fullName.isBlank()) {
            _state.value = _state.value.copy(fullNameError = "Full Name is required")
            isValid = false
        }

        if (currentState.phoneNumber.isBlank()) {
            _state.value = _state.value.copy(phoneNumberError = "Phone Number is required")
            isValid = false
        } else if (currentState.phoneNumber.length < 10) {
            _state.value = _state.value.copy(phoneNumberError = "Invalid Phone Number")
            isValid = false
        }

        if (currentState.totalClasses == null) {
            _state.value = _state.value.copy(totalClassesError = "Please select total classes")
            isValid = false
        }

        return isValid
    }
}
