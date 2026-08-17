package com.niravanadriving.app.data.repository

import com.niravanadriving.app.data.models.Vehicle
import com.niravanadriving.app.data.supabase
import io.github.jan.supabase.postgrest.postgrest

object VehicleRepository {
    suspend fun getVehicles(instructorId: String): List<Vehicle> {
        return try {
            supabase.postgrest["vehicles"].select {
                filter {
                    eq("instructor_id", instructorId)
                    eq("is_active", true)
                }
            }.decodeList<Vehicle>()
        } catch (e: Exception) {
            emptyList()
        }
    }
}
