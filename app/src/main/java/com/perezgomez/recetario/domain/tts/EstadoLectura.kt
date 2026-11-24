package com.perezgomez.recetario.domain.tts

/**
 * Representa la fase actual en el flujo de lectura de una receta.
 */
enum class EtapaLectura {
    /** El servicio está inactivo o detenido; no hay receta en lectura. */
    INACTIVO,

    /** Leyendo la introducción: título, descripción y lista de ingredientes. */
    INTRODUCCION,

    /** Leyendo un paso específico de la preparación. */
    PASO,

    /** Se han completado todos los pasos de la receta. */
    FINALIZADO,
}

/**
 * Estado observable de la lectura de recetas por voz.
 *
 * @property etapa Fase actual de la lectura.
 * @property pasoActual Índice 1-based del paso en lectura (1 .. totalPasos). 0 si no está en un paso.
 * @property totalPasos Cantidad total de pasos de preparación de la receta actual.
 * @property estaReproduciendo `true` mientras el motor TTS está emitiendo voz.
 * @property textoActual Último texto enviado al sintetizador de voz.
 * @property error Mensaje de error si ocurrió una falla en la inicialización o síntesis.
 */
data class EstadoLectura(
    val etapa: EtapaLectura = EtapaLectura.INACTIVO,
    val pasoActual: Int = 0,
    val totalPasos: Int = 0,
    val estaReproduciendo: Boolean = false,
    val textoActual: String = "",
    val error: String? = null,
) {
    /** Indica si es posible continuar hacia el siguiente paso de preparación o finalizar. */
    val puedeContinuar: Boolean
        get() = etapa == EtapaLectura.INTRODUCCION || (etapa == EtapaLectura.PASO && pasoActual <= totalPasos)

    /** Indica si es posible repetir la sección actual (introducción o paso). */
    val puedeRepetir: Boolean
        get() = etapa == EtapaLectura.INTRODUCCION || etapa == EtapaLectura.PASO

    /** Indica si la lectura ha concluido todos los pasos. */
    val esFinalizado: Boolean
        get() = etapa == EtapaLectura.FINALIZADO

    /** Indica si hay una sesión de lectura activa (no inactiva). */
    val estaActivo: Boolean
        get() = etapa != EtapaLectura.INACTIVO
}
