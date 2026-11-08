package com.perezgomez.recetario.presentation.screens

import com.perezgomez.recetario.domain.model.Receta

sealed interface DetalleContenido {
    data object Cargando : DetalleContenido
    data class Listo(val receta: Receta) : DetalleContenido
    data class Error(val mensaje: String) : DetalleContenido
}

data class DetalleUiState(
    val contenido: DetalleContenido = DetalleContenido.Cargando,
    val mensaje: String? = null,
)
