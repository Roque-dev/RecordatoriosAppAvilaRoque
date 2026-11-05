package com.example.recordatoriosappavilaroque.data

// Usamos Enums para que el formulario sea más fácil de manejar
enum class Category(val label: String) { ESTUDIO("Estudio"), HOGAR("Hogar"), TRABAJO("Trabajo") }
enum class Priority(val label: String) { ALTA("Alta"), MEDIA("Media"), BAJA("Baja") }
enum class ReminderStatus(val label: String) { PENDIENTE("Pendiente"), COMPLETADO("Completado") }

data class Reminder(
    val id: Int,
    val title: String,
    val description: String = "",
    val category: Category,
    val priority: Priority,
    val status: ReminderStatus
)