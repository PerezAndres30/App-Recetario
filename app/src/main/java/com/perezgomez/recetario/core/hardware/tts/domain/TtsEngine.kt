package com.perezgomez.recetario.core.hardware.tts.domain

/**
 * Notificador de eventos emitidos por el motor de síntesis de voz.
 */
interface TtsEventListener {
    /** Invocado cuando el motor comienza a pronunciar el texto. */
    fun onInicio()

    /** Invocado cuando el motor termina exitosamente la locución. */
    fun onFin()

    /** Invocado ante fallos de síntesis o inicialización. */
    fun onError(mensaje: String)
}

/**
 * Abstracción de bajo nivel sobre el motor de síntesis de voz (TextToSpeech).
 * Permite desacoplar la lógica de lectura de recetas del framework de Android
 * y facilita pruebas unitarias aisladas sin dependencias nativas.
 */
interface TtsEngine {
    /** Asigna o retira el receptor de eventos de locución. */
    fun setListener(listener: TtsEventListener?)

    /** Pronuncia el texto suministrado interrumpiendo cualquier locución previa. */
    fun hablar(texto: String, utteranceId: String = "")

    /** Detiene de inmediato cualquier locución en curso. */
    fun detener()

    /** Libera los recursos del sintetizador de voz (por ejemplo, shutdown). */
    fun liberar()
}
