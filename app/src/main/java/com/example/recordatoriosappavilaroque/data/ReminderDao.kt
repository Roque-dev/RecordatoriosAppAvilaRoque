package com.example.recordatoriosappavilaroque.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ReminderDao {
    @Query("SELECT * FROM reminders ORDER BY date DESC")
    fun getAllReminders(): Flow<List<ReminderEntity>>

    // Filtro por estado (Consigna 2)
    @Query("SELECT * FROM reminders WHERE status = :status")
    fun getRemindersByStatus(status: String): Flow<List<ReminderEntity>>

    @Query("SELECT * FROM reminders WHERE id = :id")
    suspend fun getReminderById(id: Int): ReminderEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: ReminderEntity): Long

    @Update
    suspend fun updateReminder(reminder: ReminderEntity)

    @Delete
    suspend fun deleteReminder(reminder: ReminderEntity)

    // Consulta para obtener pendientes de sincronizar
    @Query("SELECT * FROM reminders WHERE syncStatus = 'PENDING'")
    suspend fun getPendingReminders(): List<ReminderEntity>
}