package com.example.recordatoriosappavilaroque.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.recordatoriosappavilaroque.data.Reminder
import com.example.recordatoriosappavilaroque.data.ReminderRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Consigna 3: UiState mutuamente excluyente con sealed interface
sealed interface ListUiState {
    object Loading : ListUiState
    object Empty : ListUiState
    data class Success(val reminders: List<Reminder>) : ListUiState
    data class Error(val message: String) : ListUiState
}

class ListViewModel(private val repository: ReminderRepository) : ViewModel() {

    // Consigna 4: MutableStateFlow privado, StateFlow público de solo lectura
    private val _uiState = MutableStateFlow<ListUiState>(ListUiState.Loading)
    val uiState: StateFlow<ListUiState> = _uiState.asStateFlow()

    init {
        loadReminders()
    }

    fun loadReminders() {
        viewModelScope.launch {
            _uiState.value = ListUiState.Loading
            try {
                val data = repository.getReminders()
                if (data.isEmpty()) {
                    _uiState.value = ListUiState.Empty
                } else {
                    _uiState.value = ListUiState.Success(data)
                }
            } catch (e: Exception) {
                _uiState.value = ListUiState.Error("Error al cargar los recordatorios.")
            }
        }
    }

    // Función extra para simular el error y poder sacar la captura para el TP
    fun simulateError() {
        _uiState.value = ListUiState.Error("Fallo de red simulado. Presione reintentar.")
    }
}