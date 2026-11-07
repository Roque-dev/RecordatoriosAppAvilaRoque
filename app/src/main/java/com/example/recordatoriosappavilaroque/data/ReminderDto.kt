package com.example.recordatoriosappavilaroque.data

import com.google.gson.annotations.SerializedName

// DTO: Data Transfer Object (Lo que viaja por internet)
data class ReminderDto(
    @SerializedName("id") val id: Int? = null,
    @SerializedName("title") val title: String,
    @SerializedName("body") val description: String, // Simulamos que el servidor lo llama "body"
    @SerializedName("userId") val userId: Int = 1
)