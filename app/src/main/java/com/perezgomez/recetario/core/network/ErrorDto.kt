package com.perezgomez.recetario.core.network

import kotlinx.serialization.Serializable

/** Respuesta de error genérica devuelta por la API. */
@Serializable
data class ErrorDto(
    val error: String? = null,
)
