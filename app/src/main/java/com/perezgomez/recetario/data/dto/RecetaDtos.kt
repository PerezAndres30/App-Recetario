package com.perezgomez.recetario.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class IngredienteDto(
    val nombre: String,
    val cantidad: String = "",
    val unidad: String = "",
)

/** Lo que devuelve la API. */
@Serializable
data class RecetaDto(
    val recetaId: String,
    val titulo: String,
    val descripcion: String = "",
    val categoria: String = "otro",
    val tiempoMinutos: Int = 0,
    val porciones: Int = 1,
    val ingredientes: List<IngredienteDto> = emptyList(),
    val pasos: List<String> = emptyList(),
    val favorita: Boolean = false,
    val creadaEn: String = "",
    val actualizadaEn: String = "",
)

/** Lo que se envía a la API en POST y PUT. */
@Serializable
data class RecetaRequestDto(
    val titulo: String,
    val descripcion: String,
    val categoria: String,
    val tiempoMinutos: Int,
    val porciones: Int,
    val ingredientes: List<IngredienteDto>,
    val pasos: List<String>,
    val favorita: Boolean,
)

@Serializable
data class RecetasResponseDto(
    val recetas: List<RecetaDto>,
)
