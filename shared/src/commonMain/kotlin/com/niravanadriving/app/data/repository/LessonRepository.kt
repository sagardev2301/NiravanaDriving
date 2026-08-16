package com.niravanadriving.app.data.repository

import com.niravanadriving.app.data.models.Lesson
import com.niravanadriving.app.data.models.LessonSession
import com.niravanadriving.app.data.supabase
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

object LessonRepository {
    private fun getTodayDate(): String {
        return Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date.toString()
    }

    suspend fun getTodayLessons(instructorId: String): List<Lesson> {
        return try {
            val today = getTodayDate()
            supabase.postgrest["lessons"]
                .select(Columns.raw("*, student:students(*), vehicle:vehicles(*)")) {
                    filter {
                        eq("instructor_id", instructorId)
                        eq("scheduled_date", today)
                    }
                    order("scheduled_time", Order.ASCENDING)
                }
                .decodeList<Lesson>()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getOngoingLesson(instructorId: String): Pair<Lesson, LessonSession>? {
        return try {
            val today = getTodayDate()
            val lesson = supabase.postgrest["lessons"]
                .select(Columns.raw("*, student:students(*), vehicle:vehicles(*)")) {
                    filter {
                        eq("instructor_id", instructorId)
                        eq("scheduled_date", today)
                        eq("status", "in_progress")
                    }
                    limit(1)
                }
                .decodeSingleOrNull<Lesson>() ?: return null

            val session = supabase.postgrest["lesson_sessions"]
                .select {
                    filter {
                        eq("lesson_id", lesson.id)
                    }
                    limit(1)
                }
                .decodeSingleOrNull<LessonSession>()

            if (session != null && session.startedAt != null && session.endedAt == null) {
                Pair(lesson, session)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}
