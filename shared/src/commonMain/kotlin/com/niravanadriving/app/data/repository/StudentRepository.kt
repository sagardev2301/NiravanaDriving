package com.niravanadriving.app.data.repository

import com.niravanadriving.app.data.models.Student
import com.niravanadriving.app.data.supabase
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
private data class StudentBalanceView(
    @SerialName("student_id") val studentId: String,
    @SerialName("balance_due") val balanceDue: Double
)

@Serializable
private data class StudentProgressView(
    @SerialName("student_id") val studentId: String,
    @SerialName("sessions_completed") val sessionsCompleted: Int
)

object StudentRepository {
    suspend fun getAllStudents(instructorId: String): List<Student> {
        return try {
            val students = supabase.postgrest["students"]
                .select {
                    filter {
                        eq("instructor_id", instructorId)
                    }
                }
                .decodeList<Student>()

            if (students.isEmpty()) return emptyList()

            // Fetch balances
            val balances = supabase.postgrest["student_balance"]
                .select {
                    filter {
                        eq("instructor_id", instructorId)
                    }
                }
                .decodeList<StudentBalanceView>()
                .associateBy { it.studentId }

            // Fetch progress
            val progress = supabase.postgrest["student_lesson_progress"]
                .select {
                    filter {
                        eq("instructor_id", instructorId)
                    }
                }
                .decodeList<StudentProgressView>()
                .associateBy { it.studentId }

            // Merge
            students.forEach { student ->
                student.id?.let { id ->
                    student.balance = balances[id]?.balanceDue ?: 0.0
                    student.sessionsCompleted = progress[id]?.sessionsCompleted ?: 0
                }
            }

            students
        } catch (e: Exception) {
            emptyList()
        }
    }
}
