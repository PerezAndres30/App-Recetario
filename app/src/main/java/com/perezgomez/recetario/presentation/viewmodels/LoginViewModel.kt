package com.perezgomez.recetario.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.perezgomez.recetario.domain.repository.AuthRepository
import com.perezgomez.recetario.domain.validation.Validadores
import com.perezgomez.recetario.presentation.screens.LoginUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel(private val authRepository: AuthRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onCorreoChange(valor: String) =
        _uiState.update { it.copy(correo = valor, correoError = null, error = null) }

    fun onPasswordChange(valor: String) =
        _uiState.update { it.copy(password = valor, passwordError = null, error = null) }

    fun onIniciarSesion() {
        val actual = _uiState.value
        if (actual.cargando) return

        val correoError = Validadores.correo(actual.correo)
        val passwordError = Validadores.obligatorio(actual.password, "La contraseña")
        if (correoError != null || passwordError != null) {
            _uiState.update { it.copy(correoError = correoError, passwordError = passwordError) }
            return
        }

        _uiState.update { it.copy(cargando = true, error = null) }
        viewModelScope.launch {
            authRepository.iniciarSesion(actual.correo.trim(), actual.password)
                .onSuccess { _uiState.update { it.copy(cargando = false, autenticado = true) } }
                .onFailure { e -> _uiState.update { it.copy(cargando = false, error = e.message) } }
        }
    }
}
