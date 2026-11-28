package com.perezgomez.recetario.features.auth.data.datasources.remote.models

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
data class MigracionLikesDto(
    val intentada: Boolean = false,
    val completada: Boolean = true,
    val migrados: Int = 0,
    val deduplicados: Int = 0,
    val bloqueadaPorOtraCuenta: Boolean = false,
    val parcial: Boolean = false,
)

@Serializable
data class SesionDto(
    val token: String,
    val usuario: UsuarioDto,
    val migracionLikes: MigracionLikesDto? = null,
)

@Serializable
data class VisitanteSesionDto(
    val visitorId: String,
    val visitorToken: String,
)
