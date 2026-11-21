package com.perezgomez.recetario.domain.voice

sealed interface VoiceRecognitionState {
    data object Inactivo : VoiceRecognitionState
    data object Escuchando : VoiceRecognitionState
    data class Resultado(val texto: String) : VoiceRecognitionState
    data class Error(val mensaje: String) : VoiceRecognitionState
}
