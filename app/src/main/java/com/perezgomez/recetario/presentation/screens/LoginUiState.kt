package com.perezgomez.recetario.presentation.screens

data class LoginUiState(
    val correo: String = "",
    val password: String = "",
    val correoError: String? = null,
    val passwordError: String? = null,
    val cargando: Boolean = false,
    val error: String? = null,
    val autenticado: Boolean = false,
)
