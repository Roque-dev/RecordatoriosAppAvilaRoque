package com.example.recordatoriosappavilaroque.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reminders")
data class ReminderEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val category: String, // Guardo el enum como String
    val priority: String,
    val status: String,
    val date: Long,
    val syncStatus: String
)