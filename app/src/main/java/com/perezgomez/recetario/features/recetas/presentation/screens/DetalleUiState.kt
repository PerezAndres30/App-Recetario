package com.perezgomez.recetario.features.recetas.presentation.screens

import com.perezgomez.recetario.features.recetas.domain.entities.Receta
import com.perezgomez.recetario.features.recetas.domain.tts.EstadoLectura

sealed interface DetalleContenido {
    data object Cargando : DetalleContenido
    data class Listo(val receta: Receta) : DetalleContenido
    data class Error(val mensaje: String) : DetalleContenido
}

data class DetalleUiState(
    val contenido: DetalleContenido = DetalleContenido.Cargando,
    val estaAutenticado: Boolean = false,
    val esPropietarioVerificado: Boolean = false,
    val procesandoLike: Boolean = false,
    val procesandoFavorito: Boolean = false,
    val confirmandoEliminar: Boolean = false,
    val eliminando: Boolean = false,
    val eliminada: Boolean = false,
    val sesionExpirada: Boolean = false,
    val mensaje: String? = null,
    val ttsState: EstadoLectura = EstadoLectura(),
)
