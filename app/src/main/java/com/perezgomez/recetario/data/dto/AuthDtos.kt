package com.perezgomez.recetario.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class RegistroRequestDto(
    val nombre: String,
    val correo: String,
    val password: String,
)

@Serializable
data class LoginRequestDto(
    val correo: String,
    val password: String,
)

@Serializable
data class UsuarioDto(
    val id: String,
    val nombre: String = "",
    val correo: String = "",
)

@Serializable
data class SesionDto(
    val token: String,
    val usuario: UsuarioDto,
)

@Serializable
data class ErrorDto(
    val error: String? = null,
)
