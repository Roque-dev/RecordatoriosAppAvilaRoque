package com.example.recordatoriosappavilaroque

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.recordatoriosappavilaroque.ui.theme.RecordatoriosAppAvilaRoqueTheme

// ==========================================
// CONSIGNA 2 y 3: Modelado de datos y Colecciones
// ==========================================

// Data class inmutable para Categoría
data class Category(val name: String, val colorHex: Long)

// Sealed class para Prioridad
sealed class Priority {
    object High : Priority()
    object Medium : Priority()
    object Low : Priority()
}

// Sealed class para Estado
sealed class ReminderStatus {
    object Pending : ReminderStatus()
    object Completed : ReminderStatus()
}

// Data class para Recordatorio (con datos opcionales nullable)
data class Reminder(
    val id: Int,
    val title: String,
    val description: String? = null, // Dato opcional
    val category: Category,
    val priority: Priority,
    val status: ReminderStatus
)

// Lista de recordatorios simulados
val remindersList: List<Reminder> = listOf(
    Reminder(1, "Estudiar Kotlin", "Repasar sealed classes", Category("Estudio", 0xFF2196F3), Priority.High, ReminderStatus.Pending),
    Reminder(2, "Comprar leche", null, Category("Hogar", 0xFF4CAF50), Priority.Low, ReminderStatus.Completed),
    Reminder(3, "Hacer el TP1", "Subir el PDF", Category("Estudio", 0xFF2196F3), Priority.High, ReminderStatus.Pending),
    Reminder(4, "Pagar el internet", null, Category("Hogar", 0xFF4CAF50), Priority.Medium, ReminderStatus.Pending)
)

// Para evitar categorías repetidas
val uniqueCategories: Set<Category> = remindersList.map { it.category }.toSet()

// Map para asociar categoría con etiqueta/color
val categoryColorMap: Map<String, Long> = uniqueCategories.associate { it.name to it.colorHex }

// Filtros (Consigna 3)
val pendingReminders = remindersList.filter { it.status is ReminderStatus.Pending }
val highPriorityReminders = remindersList.filter { it.priority is Priority.High }


// ==========================================
// CONSIGNA 4: Interfaz con Jetpack Compose
// ==========================================

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RecordatoriosAppAvilaRoqueTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    // Pasamos la lista filtrada (usar remindersList para ver todos)
                    ReminderListScreen(
                        reminders = remindersList,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun ReminderListScreen(reminders: List<Reminder>, modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp)
    ) {
        items(reminders) { reminder ->
            ReminderCard(reminder)
        }
    }
}

@Composable
fun ReminderCard(reminder: Reminder) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = reminder.title, style = MaterialTheme.typography.titleLarge)

            // Mostrar descripción solo si no es null
            reminder.description?.let {
                Text(text = it, style = MaterialTheme.typography.bodyMedium)
            }

            Text(text = "Categoría: ${reminder.category.name}", style = MaterialTheme.typography.bodySmall)

            Text(text = "Prioridad: ${when(reminder.priority) {
                is Priority.High -> "Alta"
                is Priority.Medium -> "Media"
                is Priority.Low -> "Baja"
            }}", style = MaterialTheme.typography.bodySmall)

            Text(text = "Estado: ${when(reminder.status) {
                is ReminderStatus.Pending -> "Pendiente"
                is ReminderStatus.Completed -> "Completado"
            }}", style = MaterialTheme.typography.bodySmall)
        }
    }
}