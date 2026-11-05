package com.example.recordatoriosappavilaroque.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.recordatoriosappavilaroque.data.Category
import com.example.recordatoriosappavilaroque.data.Priority
import com.example.recordatoriosappavilaroque.viewmodel.CreateUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateScreen(
    uiState: CreateUiState,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onCategorySelect: (Category) -> Unit,
    onPrioritySelect: (Priority) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nuevo Recordatorio") },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Aquí está el OutlinedTextField para el Título
            OutlinedTextField(
                value = uiState.title,
                onValueChange = onTitleChange,
                label = { Text("Título") },
                isError = uiState.titleError != null,
                supportingText = { uiState.titleError?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
                modifier = Modifier.fillMaxWidth()
            )

            // Aquí está el OutlinedTextField para la Descripción
            OutlinedTextField(
                value = uiState.description,
                onValueChange = onDescriptionChange,
                label = { Text("Descripción (opcional)") },
                isError = uiState.descriptionError != null,
                supportingText = { uiState.descriptionError?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            // Aquí están los FilterChips para Categoría
            Text("Categoría:", style = MaterialTheme.typography.titleSmall)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Category.entries.forEach { category ->
                    FilterChip(
                        selected = uiState.category == category,
                        onClick = { onCategorySelect(category) },
                        label = { Text(category.label) }
                    )
                }
            }

            // Aquí están los FilterChips para Prioridad
            Text("Prioridad:", style = MaterialTheme.typography.titleSmall)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Priority.entries.forEach { priority ->
                    FilterChip(
                        selected = uiState.priority == priority,
                        onClick = { onPrioritySelect(priority) },
                        label = { Text(priority.label) }
                    )
                }
            }

            // Mensaje de error general
            uiState.generalError?.let {
                Text(text = it, color = MaterialTheme.colorScheme.error)
            }

            Spacer(modifier = Modifier.weight(1f))

            // Botón Guardar
            Button(
                onClick = onSave,
                enabled = uiState.isFormValid && !uiState.isSubmitting,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (uiState.isSubmitting) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("Guardar Recordatorio")
                }
            }
        }
    }
}