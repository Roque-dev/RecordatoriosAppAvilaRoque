package com.example.recordatoriosappavilaroque.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.recordatoriosappavilaroque.data.AuthRepository
import com.example.recordatoriosappavilaroque.data.Reminder
import com.example.recordatoriosappavilaroque.data.ReminderRepository
import com.example.recordatoriosappavilaroque.data.PreferencesRepository
import com.example.recordatoriosappavilaroque.data.SortOrder
import com.example.recordatoriosappavilaroque.data.ReminderStatus
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed interface ListUiState {
    object Loading : ListUiState
    object Empty : ListUiState
    data class Success(val reminders: List<Reminder>) : ListUiState
    data class Error(val message: String) : ListUiState
}

class ListViewModel(
    private val repository: ReminderRepository,
    private val preferencesRepository: PreferencesRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    val uiState: StateFlow<ListUiState> = combine(
        repository.getReminders(),
        preferencesRepository.userPreferencesFlow
    ) { reminders, prefs ->
        var filtered = if (prefs.showCompleted) {
            reminders
        } else {
            reminders.filter { it.status != ReminderStatus.COMPLETADO }
        }

        filtered = when (prefs.sortOrder) {
            SortOrder.BY_DATE -> filtered.sortedByDescending { it.date }
            SortOrder.BY_PRIORITY -> filtered.sortedBy { it.priority.ordinal }
        }

        if (filtered.isEmpty()) {
            ListUiState.Empty as ListUiState
        } else {
            ListUiState.Success(filtered) as ListUiState
        }
    }.catch { e ->
        emit(ListUiState.Error("Error al leer la base de datos: ${e.message}"))
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ListUiState.Loading
    )

    fun syncData() {
        viewModelScope.launch {
            _isSyncing.value = true
            val result = repository.syncWithRemote()
            _isSyncing.value = false
        }
    }

    fun logout(onLogoutSuccess: () -> Unit) {
        viewModelScope.launch {
            authRepository.logout()
            repository.clearLocalData()
            onLogoutSuccess()
        }
    }

    fun getUserEmail(): String? {
        return authRepository.currentUser?.email
    }
}