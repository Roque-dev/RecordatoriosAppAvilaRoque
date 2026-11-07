package com.example.recordatoriosappavilaroque.data

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.IOException

class OfflineReminderRepository(
    private val dao: ReminderDao,
    private val apiService: ReminderApiService
) : ReminderRepository {

    // 1. LEER: Siempre se lee de Room. La UI es feliz y funciona offline.
    override fun getReminders(): Flow<List<Reminder>> {
        return dao.getAllReminders().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getReminderById(id: Int): Reminder? = withContext(Dispatchers.IO) {
        dao.getReminderById(id)?.toDomain()
    }

    // 2. GUARDAR: Guardo primero en Room (Local-First).
    override suspend fun saveReminder(reminder: Reminder): Long = withContext(Dispatchers.IO) {
        // Lo guardamos como PENDING por si se corta internet
        val entity = reminder.copy(syncStatus = SyncStatus.PENDING).toEntity()
        val id = dao.insertReminder(entity)

        // Intenta sincronizar inmediatamente en segundo plano (si hay red)
        syncWithRemote()

        return@withContext id
    }

    override suspend fun deleteReminder(reminder: Reminder) = withContext(Dispatchers.IO) {
        dao.deleteReminder(reminder.toEntity())
        // Nota mia: En una app real, aca también avisaría al servidor que lo borre.
    }

    // 3. SINCRONIZAR: La función más importante
    override suspend fun syncWithRemote(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // A. Subir los pendientes locales (los creados offline)
            val pendingReminders = dao.getPendingReminders()
            for (pending in pendingReminders) {
                try {
                    val remoteDto = apiService.createRemoteReminder(pending.toDto())
                    // Si el servidor responde bien, lo actualizo a SYNCED
                    val updatedEntity = pending.copy(
                        id = remoteDto.id ?: pending.id,
                        syncStatus = SyncStatus.SYNCED.name
                    )
                    dao.updateReminder(updatedEntity)
                } catch (e: Exception) {
                    Log.e("Sync", "Error subiendo pendiente", e)
                }
            }

            // B. Descargar los remotos y actualizar Room (evitando duplicados)
            val remoteDtos = apiService.getRemoteReminders()
            // Tomo solo los primeros 5 de la API de prueba para no llenar la base de basura
            remoteDtos.take(5).forEach { dto ->
                val entity = dto.toEntity()
                // Inserto con REPLACE. Si el ID ya existe, lo actualiza. No duplica.
                dao.insertReminder(entity)
            }

            Result.success(Unit)
        } catch (e: IOException) {
            // Error de red (sin internet)
            Log.e("Sync", "Sin conexión a internet", e)
            Result.failure(e)
        } catch (e: Exception) {
            // Error del servidor
            Log.e("Sync", "Error en el servidor", e)
            Result.failure(e)
        }
    }
}