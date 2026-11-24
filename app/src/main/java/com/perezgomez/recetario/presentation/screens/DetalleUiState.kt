package com.perezgomez.recetario.presentation.screens

import com.perezgomez.recetario.domain.model.Receta
import com.perezgomez.recetario.domain.tts.EstadoLectura

sealed interface DetalleContenido {
    data object Cargando : DetalleContenido
    data class Listo(val receta: Receta) : DetalleContenido
    data class Error(val mensaje: String) : DetalleContenido
}

data class DetalleUiState(
    val contenido: DetalleContenido = DetalleContenido.Cargando,
    val esPropietarioVerificado: Boolean = false,
    val confirmandoEliminar: Boolean = false,
    val eliminando: Boolean = false,
    val eliminada: Boolean = false,
    val mensaje: String? = null,
    val ttsState: EstadoLectura = EstadoLectura(),
)
