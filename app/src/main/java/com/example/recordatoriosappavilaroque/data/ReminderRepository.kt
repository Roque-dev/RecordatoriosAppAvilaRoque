package com.example.recordatoriosappavilaroque.data

import kotlinx.coroutines.flow.Flow

interface ReminderRepository {
    // La UI ve esto (viene directo de Room)
    fun getReminders(): Flow<List<Reminder>>

    suspend fun getReminderById(id: Int): Reminder?
    suspend fun saveReminder(reminder: Reminder): Long
    suspend fun deleteReminder(reminder: Reminder)

    // Función para forzar la sincronización (la llama desde la UI)
    suspend fun syncWithRemote(): Result<Unit>

    // Función para limpiar los datos del usuario
    suspend fun clearLocalData()
}