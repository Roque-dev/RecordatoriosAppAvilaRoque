package com.example.recordatoriosappavilaroque.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.recordatoriosappavilaroque.data.Reminder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(reminder: Reminder?, onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { innerPadding ->
        if (reminder == null) {
            Box(modifier = Modifier.padding(innerPadding).fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Recordatorio no encontrado")
            }
        } else {
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .padding(16.dp)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(text = reminder.title, style = MaterialTheme.typography.headlineMedium)
                Text(text = "Categoría: ${reminder.category.label}", style = MaterialTheme.typography.bodyLarge)
                Text(text = "Prioridad: ${reminder.priority.label}", style = MaterialTheme.typography.bodyLarge)
                Text(text = "Estado: ${reminder.status.label}", style = MaterialTheme.typography.bodyLarge)
                Divider()
                Text(text = "Descripción:", style = MaterialTheme.typography.titleMedium)
                Text(text = reminder.description.ifBlank { "Sin descripción." }, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}