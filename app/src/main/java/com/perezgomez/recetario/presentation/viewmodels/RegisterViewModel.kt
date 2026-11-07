package com.perezgomez.recetario.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.perezgomez.recetario.domain.repository.AuthRepository
import com.perezgomez.recetario.domain.validation.Validadores
import com.perezgomez.recetario.presentation.screens.RegisterUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RegisterViewModel(private val authRepository: AuthRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun onNombreChange(valor: String) =
        _uiState.update { it.copy(nombre = valor, nombreError = null, error = null) }

    fun onCorreoChange(valor: String) =
        _uiState.update { it.copy(correo = valor, correoError = null, error = null) }

    fun onPasswordChange(valor: String) =
        _uiState.update { it.copy(password = valor, passwordError = null, error = null) }

    fun onConfirmacionChange(valor: String) =
        _uiState.update { it.copy(confirmacion = valor, confirmacionError = null, error = null) }

    fun onRegistrar() {
        val actual = _uiState.value
        if (actual.cargando) return

        val nombreError = Validadores.obligatorio(actual.nombre, "El nombre")
            ?: Validadores.longitudMaxima(actual.nombre.trim(), "El nombre", 80)
        val correoError = Validadores.correo(actual.correo)
        val passwordError = Validadores.password(actual.password)
        val confirmacionError = Validadores.confirmacion(actual.password, actual.confirmacion)

        if (listOf(nombreError, correoError, passwordError, confirmacionError).any { it != null }) {
            _uiState.update {
                it.copy(
                    nombreError = nombreError,
                    correoError = correoError,
                    passwordError = passwordError,
                    confirmacionError = confirmacionError,
                )
            }
            return
        }

        _uiState.update { it.copy(cargando = true, error = null) }
        viewModelScope.launch {
            authRepository.registrar(actual.nombre.trim(), actual.correo.trim(), actual.password)
                .onSuccess { _uiState.update { it.copy(cargando = false, registrado = true) } }
                .onFailure { e -> _uiState.update { it.copy(cargando = false, error = e.message) } }
        }
    }
}
