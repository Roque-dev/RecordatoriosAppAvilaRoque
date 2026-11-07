package com.example.recordatoriosappavilaroque.data

// Estados de sincronización
enum class SyncStatus { SYNCED, PENDING }

enum class Category(val label: String) { ESTUDIO("Estudio"), HOGAR("Hogar"), TRABAJO("Trabajo") }
enum class Priority(val label: String) { ALTA("Alta"), MEDIA("Media"), BAJA("Baja") }
enum class ReminderStatus(val label: String) { PENDIENTE("Pendiente"), COMPLETADO("Completado") }

data class Reminder(
    val id: Int = 0,
    val title: String,
    val description: String = "",
    val category: Category,
    val priority: Priority,
    val status: ReminderStatus,
    val date: Long = System.currentTimeMillis(), // Fecha de creación
    val syncStatus: SyncStatus = SyncStatus.SYNCED // Para el modo offline
)