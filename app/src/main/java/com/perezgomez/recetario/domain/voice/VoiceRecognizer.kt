package com.perezgomez.recetario.domain.voice

import kotlinx.coroutines.flow.StateFlow

interface VoiceRecognizer {
    val state: StateFlow<VoiceRecognitionState>
    fun iniciar(idioma: String = "es-ES")
    fun detener()
    fun limpiar()
}
