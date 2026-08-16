package com.niravanadriving.app.data.repository

import com.niravanadriving.app.data.models.Instructor
import com.niravanadriving.app.data.supabase
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest

object InstructorRepository {
    suspend fun getCurrentInstructor(): Instructor? {
        return try {
            val authUserId = supabase.auth.currentUserOrNull()?.id ?: return null
            supabase.postgrest["instructors"]
                .select {
                    filter {
                        eq("auth_user_id", authUserId)
                    }
                }
                .decodeSingle<Instructor>()
        } catch (e: Exception) {
            null
        }
    }
}
