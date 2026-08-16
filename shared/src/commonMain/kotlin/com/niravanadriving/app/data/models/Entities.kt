package com.niravanadriving.app.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
enum class LessonStatus {
    @SerialName("scheduled") SCHEDULED,
    @SerialName("in_progress") IN_PROGRESS,
    @SerialName("completed") COMPLETED,
    @SerialName("cancelled") CANCELLED
}

@Serializable
enum class PaymentStatus {
    @SerialName("pending") PENDING,
    @SerialName("paid") PAID,
    @SerialName("refunded") REFUNDED
}

@Serializable
enum class PaymentMethod {
    @SerialName("cash") CASH,
    @SerialName("upi") UPI,
    @SerialName("card") CARD,
    @SerialName("bank_transfer") BANK_TRANSFER
}

@Serializable
enum class LicenseType {
    @SerialName("learner") LEARNER,
    @SerialName("provisional") PROVISIONAL,
    @SerialName("full") FULL
}

@Serializable
enum class TransmissionType {
    @SerialName("manual") MANUAL,
    @SerialName("automatic") AUTOMATIC,
    @SerialName("both") BOTH
}

@Serializable
data class Instructor(
    val id: String,
    @SerialName("auth_user_id") val authUserId: String,
    @SerialName("full_name") val fullName: String,
    val phone: String? = null,
    @SerialName("license_number") val licenseNumber: String? = null,
    @SerialName("profile_photo_url") val profilePhotoUrl: String? = null,
    val email: String? = null,
    @SerialName("is_active") val isActive: Boolean = true,
    val title: String? = null,
    @SerialName("years_experience") val yearsExperience: Int? = null,
    val grade: String? = null
)

@Serializable
data class Student(
    val id: String? = null,
    @SerialName("instructor_id") val instructorId: String,
    @SerialName("full_name") val fullName: String,
    val phone: String? = null,
    val email: String? = null,
    @SerialName("date_of_birth") val dateOfBirth: String? = null,
    @SerialName("license_type") val licenseType: LicenseType? = null,
    @SerialName("profile_photo_url") val profilePhotoUrl: String? = null,
    val address: String? = null,
    @SerialName("fee_per_session") val feePerSession: Double? = null,
    @SerialName("total_sessions") val totalSessions: Int? = 15,
    @SerialName("instructor_remarks") val instructorRemarks: String? = null,
    @SerialName("is_active") val isActive: Boolean = true
) {
    @Transient var sessionsCompleted: Int = 0
    @Transient var balance: Double = 0.0
}

@Serializable
data class Vehicle(
    val id: String,
    @SerialName("instructor_id") val instructorId: String,
    @SerialName("make_model") val makeModel: String,
    @SerialName("registration_number") val registrationNumber: String,
    @SerialName("transmission_type") val transmissionType: TransmissionType,
    @SerialName("is_active") val isActive: Boolean = true
)

@Serializable
data class Lesson(
    val id: String,
    @SerialName("instructor_id") val instructorId: String,
    @SerialName("student_id") val studentId: String,
    @SerialName("scheduled_date") val scheduledDate: String,
    @SerialName("scheduled_time") val scheduledTime: String,
    @SerialName("duration_minutes") val durationMinutes: Int,
    val status: LessonStatus,
    @SerialName("pickup_location") val pickupLocation: String? = null,
    val route: String? = null,
    val notes: String? = null,
    @SerialName("vehicle_id") val vehicleId: String? = null,
    // Helper fields for UI (to be populated via Supabase Joins)
    val student: Student? = null,
    val vehicle: Vehicle? = null
)

@Serializable
data class LessonSession(
    val id: String,
    @SerialName("lesson_id") val lessonId: String,
    @SerialName("started_at") val startedAt: String,
    @SerialName("ended_at") val endedAt: String? = null,
    @SerialName("actual_duration_minutes") val actualDurationMinutes: Int? = null,
    @SerialName("instructor_notes") val instructorNotes: String? = null,
    @SerialName("overall_rating") val overallRating: Int? = null
)

@Serializable
data class Payment(
    val id: String? = null,
    @SerialName("lesson_id") val lessonId: String? = null,
    @SerialName("student_id") val studentId: String,
    @SerialName("instructor_id") val instructorId: String,
    val amount: Double,
    val currency: String = "INR",
    @SerialName("payment_method") val paymentMethod: PaymentMethod,
    val status: PaymentStatus,
    @SerialName("paid_at") val paidAt: String? = null,
    val notes: String? = null
)
