package com.example.recordatoriosappavilaroque.data

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class OfflineReminderRepository(
    private val dao: ReminderDao,
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : ReminderRepository {

    override fun getReminders(): Flow<List<Reminder>> {
        return dao.getAllReminders().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getReminderById(id: Int): Reminder? = withContext(Dispatchers.IO) {
        dao.getReminderById(id)?.toDomain()
    }

    override suspend fun saveReminder(reminder: Reminder): Long = withContext(Dispatchers.IO) {
        // Guarda como PENDING (Local-First)
        val entity = reminder.copy(syncStatus = SyncStatus.PENDING).toEntity()
        val id = dao.insertReminder(entity)

        // Intenta sincronizar con Firebase en segundo plano
        syncWithRemote()

        return@withContext id
    }

    override suspend fun deleteReminder(reminder: Reminder) = withContext(Dispatchers.IO) {
        dao.deleteReminder(reminder.toEntity())
    }

    override suspend fun syncWithRemote(): Result<Unit> = withContext(Dispatchers.IO) {
        val userId = auth.currentUser?.uid ?: return@withContext Result.failure(Exception("No hay usuario autenticado"))

        // Ruta: users / uid / reminders
        val userRemindersRef = firestore.collection("users").document(userId).collection("reminders")

        try {
            // A. Subir pendientes locales a Firestore
            val pendingReminders = dao.getPendingReminders()
            for (pending in pendingReminders) {
                val data = mapOf(
                    "title" to pending.title,
                    "description" to pending.description,
                    "category" to pending.category,
                    "priority" to pending.priority,
                    "status" to pending.status,
                    "date" to pending.date
                )
                // Usa el ID de Room como ID del documento en Firestore para evitar duplicados
                userRemindersRef.document(pending.id.toString()).set(data).await()

                // Una vez subido, lo marca como SYNCED
                dao.updateReminder(pending.copy(syncStatus = SyncStatus.SYNCED.name))
            }

            // B. Descargar datos remotos y actualizar Room
            val snapshot = userRemindersRef.get().await()
            for (doc in snapshot.documents) {
                val id = doc.id.toIntOrNull() ?: 0
                if (id > 0) {
                    val entity = ReminderEntity(
                        id = id,
                        title = doc.getString("title") ?: "",
                        description = doc.getString("description") ?: "",
                        category = doc.getString("category") ?: "HOGAR",
                        priority = doc.getString("priority") ?: "MEDIA",
                        status = doc.getString("status") ?: "PENDIENTE",
                        date = doc.getLong("date") ?: System.currentTimeMillis(),
                        syncStatus = SyncStatus.SYNCED.name
                    )
                    // Inserta con REPLACE para no duplicar
                    dao.insertReminder(entity)
                }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("Sync", "Error en Firebase", e)
            Result.failure(e)
        }
    }

    override suspend fun clearLocalData() = withContext(Dispatchers.IO) {
        dao.clearAllReminders()
    }
}