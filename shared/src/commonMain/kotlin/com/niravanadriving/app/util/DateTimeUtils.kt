package com.niravanadriving.app.util

import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.toInstant
import kotlin.time.Duration
import kotlin.time.Clock

object DateTimeUtils {
    fun formatTime(timeString: String): String {
        if (timeString.isBlank()) return ""
        if (timeString.contains("AM") || timeString.contains("PM")) return timeString
        val parts = timeString.split(":")
        if (parts.size < 2) return timeString
        val hour = parts[0].toIntOrNull() ?: return timeString
        val minute = parts[1].toIntOrNull() ?: 0
        val amPm = if (hour < 12) "AM" else "PM"
        val displayHour = when {
            hour == 0 -> 12
            hour > 12 -> hour - 12
            else -> hour
        }
        return "${displayHour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')} $amPm"
    }

    fun calculateEndTime(startTimeString: String, durationMinutes: Int): String {
        if (startTimeString.isBlank()) return ""
        val parts = startTimeString.split(":")
        if (parts.size < 2) return startTimeString
        val hour = parts[0].toIntOrNull() ?: return startTimeString
        val minute = parts[1].toIntOrNull() ?: 0
        
        val totalMinutes = hour * 60 + minute + durationMinutes
        val endHour = (totalMinutes / 60) % 24
        val endMinute = totalMinutes % 60
        
        val amPm = if (endHour < 12) "AM" else "PM"
        val displayHour = when {
            endHour == 0 -> 12
            endHour > 12 -> endHour - 12
            else -> endHour
        }
        return "${displayHour.toString().padStart(2, '0')}:${endMinute.toString().padStart(2, '0')} $amPm"
    }

    fun getMinutesUntil(timeString: String): Long {
        if (timeString.isBlank()) return -1
        val parts = timeString.split(":")
        if (parts.size < 2) return -1
        
        return try {
            val hour = parts[0].toIntOrNull() ?: return -1
            val minute = parts[1].toIntOrNull() ?: 0
            
            val now = Clock.System.now()
            val nowLocal = now.toLocalDateTime(TimeZone.currentSystemDefault())
            val scheduled = LocalDateTime(nowLocal.year, nowLocal.month, nowLocal.day, hour, minute)
            
            val scheduledInstant = scheduled.toInstant(TimeZone.currentSystemDefault())
            val diff: Duration = scheduledInstant - now
            
            diff.inWholeMinutes
        } catch (e: Exception) {
            -1
        }
    }
}
