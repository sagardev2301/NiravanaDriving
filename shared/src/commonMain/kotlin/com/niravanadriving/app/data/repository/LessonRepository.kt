package com.niravanadriving.app.data.repository

import com.niravanadriving.app.data.models.Lesson
import com.niravanadriving.app.data.models.LessonSession
import com.niravanadriving.app.data.models.LessonStatus
import com.niravanadriving.app.data.supabase
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.DurationUnit

object LessonRepository {
    private fun getTodayDate(): LocalDate {
        return Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    }

    suspend fun getTodayLessons(instructorId: String): List<Lesson> {
        return try {
            val today = getTodayDate()
            supabase.postgrest["lessons"]
                .select(Columns.raw("*, student:students(*), vehicle:vehicles(*)")) {
                    filter {
                        eq("instructor_id", instructorId)
                        eq("scheduled_date", today.toString())
                        eq("is_draft", false)
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
                        eq("scheduled_date", today.toString())
                        eq("status", "in_progress")
                        eq("is_draft", false)
                    }
                    limit(1)
                }
                .decodeSingleOrNull<Lesson>() ?: return null

            val session = supabase.postgrest["lesson_sessions"]
                .select {
                    filter {
                        eq("lesson_id", lesson.id!!)
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

    suspend fun getLessonsForDate(instructorId: String, date: LocalDate): List<Lesson> {
        return try {
            supabase.postgrest["lessons"]
                .select(Columns.raw("*, student:students(*), vehicle:vehicles(*)")) {
                    filter {
                        eq("instructor_id", instructorId)
                        eq("scheduled_date", date.toString())
                    }
                    order("scheduled_time", Order.ASCENDING)
                }
                .decodeList<Lesson>()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getYesterdayPublishedLessons(instructorId: String): List<Lesson> {
        val yesterday = getTodayDate().minus(1, DateTimeUnit.DAY)
        return getPublishedLessonsForDate(instructorId, yesterday)
    }

    suspend fun getPublishedLessonsForDate(instructorId: String, date: LocalDate): List<Lesson> {
        return try {
            supabase.postgrest["lessons"]
                .select(Columns.raw("*, student:students(*), vehicle:vehicles(*)")) {
                    filter {
                        eq("instructor_id", instructorId)
                        eq("scheduled_date", date.toString())
                        eq("is_draft", false)
                    }
                    order("scheduled_time", Order.ASCENDING)
                }
                .decodeList<Lesson>()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun saveDraftLessons(lessons: List<Lesson>): Boolean {
        return try {
            if (lessons.isEmpty()) return true
            // Setting is_draft to true for all before upserting
            val drafts = lessons.map { it.copy(isDraft = true) }
            supabase.postgrest["lessons"].upsert(drafts)
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun publishLessonsForDate(instructorId: String, date: LocalDate): Boolean {
        return try {
            supabase.postgrest["lessons"].update(
                {
                    Lesson::isDraft setTo false
                }
            ) {
                filter {
                    eq("instructor_id", instructorId)
                    eq("scheduled_date", date.toString())
                    eq("is_draft", true)
                }
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun deleteLesson(lessonId: String): Boolean {
        return try {
            supabase.postgrest["lessons"].delete {
                filter {
                    eq("id", lessonId)
                }
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun startLesson(lessonId: String): LessonSession? {
        return try {
            // Update lesson status
            supabase.postgrest["lessons"].update(
                {
                    Lesson::status setTo LessonStatus.IN_PROGRESS
                }
            ) {
                filter { eq("id", lessonId) }
            }

            // Insert new session
            val now = Clock.System.now().toString()
            supabase.postgrest["lesson_sessions"].insert(
                mapOf(
                    "lesson_id" to lessonId,
                    "started_at" to now
                )
            ) {
                select()
            }.decodeSingle<LessonSession>()
        } catch (e: Exception) {
            null
        }
    }

    suspend fun endLesson(lessonId: String, sessionId: String): Boolean {
        return try {
            val now = Clock.System.now()
            
            // 1. Fetch session to get started_at
            val session = supabase.postgrest["lesson_sessions"]
                .select { filter { eq("id", sessionId) } }
                .decodeSingle<LessonSession>()
            
            val startedAt = Instant.parse(session.startedAt)
            val durationMinutes = (now - startedAt).toInt(DurationUnit.MINUTES)

            // 2. Update session
            supabase.postgrest["lesson_sessions"].update(
                {
                    LessonSession::endedAt setTo now.toString()
                    LessonSession::actualDurationMinutes setTo durationMinutes
                }
            ) {
                filter { eq("id", sessionId) }
            }

            // 3. Update lesson status
            supabase.postgrest["lessons"].update(
                {
                    Lesson::status setTo LessonStatus.COMPLETED
                }
            ) {
                filter { eq("id", lessonId) }
            }
            true
        } catch (e: Exception) {
            false
        }
    }
}
