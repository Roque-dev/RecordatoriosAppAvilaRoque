package com.example.recordatoriosappavilaroque.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.recordatoriosappavilaroque.data.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CreateUiState(
    val title: String = "",
    val description: String = "",
    val category: Category? = null,
    val priority: Priority? = null,
    val titleError: String? = null,
    val descriptionError: String? = null,
    val isSubmitting: Boolean = false,
    val saveSuccess: Boolean = false,
    val generalError: String? = null
) {
    // Consigna 2: Reglas de validación
    val isFormValid: Boolean
        get() = title.trim().length >= 4 &&
                description.length <= 200 &&
                category != null &&
                priority != null
}

class CreateViewModel(private val repository: ReminderRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(CreateUiState())
    val uiState: StateFlow<CreateUiState> = _uiState.asStateFlow()

    fun onTitleChange(title: String) {
        _uiState.update {
            it.copy(
                title = title,
                titleError = if (title.trim().length in 1..3) "Mínimo 4 caracteres" else null
            )
        }
    }

    fun onDescriptionChange(desc: String) {
        _uiState.update {
            it.copy(
                description = desc,
                descriptionError = if (desc.length > 200) "Máximo 200 caracteres" else null
            )
        }
    }

    fun onCategorySelect(category: Category) {
        _uiState.update { it.copy(category = category) }
    }

    fun onPrioritySelect(priority: Priority) {
        _uiState.update { it.copy(priority = priority) }
    }

    fun saveReminder() {
        val currentState = _uiState.value
        if (!currentState.isFormValid || currentState.isSubmitting) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, generalError = null) }
            try {
                repository.saveReminder(
                    Reminder(
                        id = 0,
                        title = currentState.title.trim(),
                        description = currentState.description,
                        category = currentState.category!!,
                        priority = currentState.priority!!,
                        status = ReminderStatus.PENDIENTE
                    )
                )
                _uiState.update { it.copy(isSubmitting = false, saveSuccess = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSubmitting = false, generalError = "No se pudo guardar.") }
            }
        }
    }
}