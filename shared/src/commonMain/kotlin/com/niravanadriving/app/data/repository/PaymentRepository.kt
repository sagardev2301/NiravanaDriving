package com.niravanadriving.app.data.repository

import com.niravanadriving.app.data.models.Payment
import com.niravanadriving.app.data.supabase
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

object PaymentRepository {
    suspend fun getTodayCollections(instructorId: String): Double {
        return try {
            val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date.toString()
            val payments = supabase.postgrest["payments"].select {
                filter {
                    eq("instructor_id", instructorId)
                    eq("status", "paid")
                    gte("paid_at", today)
                }
            }.decodeList<Payment>()
            
            payments.filter { it.paidAt?.startsWith(today) == true }.sumOf { it.amount }
        } catch (e: Exception) {
            0.0
        }
    }
}
