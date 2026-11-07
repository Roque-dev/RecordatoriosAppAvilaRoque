package com.example.recordatoriosappavilaroque.data

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ReminderApiService {
    // Simulo una API pública (como JSONPlaceholder)
    @GET("posts")
    suspend fun getRemoteReminders(): List<ReminderDto>

    @POST("posts")
    suspend fun createRemoteReminder(@Body reminderDto: ReminderDto): ReminderDto
}