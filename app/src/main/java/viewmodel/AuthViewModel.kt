package com.example.recordatoriosappavilaroque.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.recordatoriosappavilaroque.data.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuthUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false,
    val isRegisterMode: Boolean = false // Para alternar entre Login y Registro
)

class AuthViewModel(private val repository: AuthRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun onEmailChange(email: String) { _uiState.update { it.copy(email = email, error = null) } }
    fun onPasswordChange(password: String) { _uiState.update { it.copy(password = password, error = null) } }
    fun toggleMode() { _uiState.update { it.copy(isRegisterMode = !it.isRegisterMode, error = null) } }

    fun authenticate() {
        val state = _uiState.value
        if (state.email.isBlank() || state.password.isBlank()) {
            _uiState.update { it.copy(error = "El correo y la contraseña son obligatorios") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val result = if (state.isRegisterMode) {
                repository.register(state.email, state.password)
            } else {
                repository.login(state.email, state.password)
            }

            result.onSuccess {
                _uiState.update { it.copy(isLoading = false, isSuccess = true) }
            }.onFailure { exception ->
                _uiState.update { it.copy(isLoading = false, error = exception.message ?: "Error de autenticación") }
            }
        }
    }
}