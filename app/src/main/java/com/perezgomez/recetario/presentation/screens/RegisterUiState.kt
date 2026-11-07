package com.perezgomez.recetario.presentation.screens

data class RegisterUiState(
    val nombre: String = "",
    val correo: String = "",
    val password: String = "",
    val confirmacion: String = "",
    val nombreError: String? = null,
    val correoError: String? = null,
    val passwordError: String? = null,
    val confirmacionError: String? = null,
    val cargando: Boolean = false,
    val error: String? = null,
    val registrado: Boolean = false,
)
