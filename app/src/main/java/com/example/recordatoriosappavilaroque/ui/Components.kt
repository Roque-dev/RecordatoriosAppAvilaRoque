package com.example.recordatoriosappavilaroque.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.recordatoriosappavilaroque.data.Reminder

// Consigna 2: Componente reutilizable con parámetro modifier por defecto
@Composable
fun ReminderCard(
    reminder: Reminder,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = onClick // Hacemos que la tarjeta sea clickeable
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = reminder.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "Categoría: ${reminder.category.label}", style = MaterialTheme.typography.bodySmall)
            Text(text = "Prioridad: ${reminder.priority.label}", style = MaterialTheme.typography.bodySmall)
            Text(text = "Estado: ${reminder.status.label}", style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
fun EmptyState(onCreate: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "No hay recordatorios aún.", style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onCreate) {
            Text("Crear mi primer recordatorio")
        }
    }
}

@Composable
fun ErrorState(message: String, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = message, color = MaterialTheme.colorScheme.error)
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRetry) {
            Text("Reintentar")
        }
    }
}