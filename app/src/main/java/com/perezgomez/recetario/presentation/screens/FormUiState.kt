package com.perezgomez.recetario.presentation.screens

import com.perezgomez.recetario.domain.voice.VoiceRecognitionState

sealed interface CampoDictado {
    data object Descripcion : CampoDictado
    data class IngredienteNombre(val indice: Int) : CampoDictado
    data class Paso(val indice: Int) : CampoDictado
}

data class IngredienteForm(
    val nombre: String = "",
    val cantidad: String = "",
    val unidad: String = "",
)

data class FormErrores(
    val titulo: String? = null,
    val descripcion: String? = null,
    val categoria: String? = null,
    val tiempo: String? = null,
    val porciones: String? = null,
    val ingredientes: String? = null,
    val pasos: String? = null,
) {
    fun hayErrores() = listOf(titulo, descripcion, categoria, tiempo, porciones, ingredientes, pasos).any { it != null }
}

data class FormUiState(
    val esEdicion: Boolean = false,
    val cargando: Boolean = false,
    val guardando: Boolean = false,
    val titulo: String = "",
    val descripcion: String = "",
    val categoria: String = "",
    val tiempoMinutos: String = "",
    val porciones: String = "1",
    val ingredientes: List<IngredienteForm> = listOf(IngredienteForm()),
    val pasos: List<String> = listOf(""),
    val favorita: Boolean = false,
    val errores: FormErrores = FormErrores(),
    val error: String? = null,
    val guardado: Boolean = false,
    val mensajeExito: String = "",
    val campoDictadoActivo: CampoDictado? = null,
    val estadoDictado: VoiceRecognitionState = VoiceRecognitionState.Inactivo,
    val mensajeDictado: String? = null,
)
