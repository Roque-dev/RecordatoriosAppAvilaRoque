package com.example.recordatoriosappavilaroque.data

import kotlinx.coroutines.delay

interface ReminderRepository {
    suspend fun getReminders(): List<Reminder>
    suspend fun saveReminder(reminder: Reminder)
}

class InMemoryReminderRepository : ReminderRepository {
    // Simulamos una base de datos en memoria con 2 recordatorios iniciales
    private val reminders = mutableListOf<Reminder>(
        Reminder(1, "Estudiar Kotlin", "Repasar sealed classes", Category.ESTUDIO, Priority.ALTA, ReminderStatus.PENDIENTE),
        Reminder(2, "Comprar leche", "", Category.HOGAR, Priority.BAJA, ReminderStatus.COMPLETADO)
    )
    private var nextId = 3

    override suspend fun getReminders(): List<Reminder> {
        delay(1500) // Simulamos que tarda en cargar (para ver el estado Loading)
        return reminders.toList()
    }

    override suspend fun saveReminder(reminder: Reminder) {
        delay(1000) // Simulamos que tarda en guardar
        reminders.add(reminder.copy(id = nextId++))
    }
    fun getRemindersSync(): List<Reminder> = reminders.toList()
}