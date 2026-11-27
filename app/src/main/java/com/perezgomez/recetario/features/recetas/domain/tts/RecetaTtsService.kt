package com.perezgomez.recetario.features.recetas.domain.tts

import com.perezgomez.recetario.features.recetas.domain.entities.Receta
import kotlinx.coroutines.flow.StateFlow

/**
 * Servicio de dominio responsable de orquestar la lectura asistida por voz de una receta
 * en sus distintas etapas: introducción, pasos de preparación secuenciales, repetición y finalización.
 */
interface RecetaTtsService {

    /** Flujo observable con el estado actual de la lectura. */
    val estado: StateFlow<EstadoLectura>

    /**
     * Inicia o reinicia la lectura de la receta desde la introducción:
     * lee en voz alta el título, la descripción y los ingredientes.
     */
    fun reproducir(receta: Receta)

    /**
     * Avanza al siguiente paso de preparación y lo lee en voz alta.
     * Si ya se leyó el último paso disponible, anuncia que la receta ha concluido.
     */
    fun continuar()

    /**
     * Repite la sección actual: la introducción completa o el paso actual.
     */
    fun repetir()

    /**
     * Detiene la locución actual de inmediato sin destruir el estado de la receta.
     */
    fun detener()

    /**
     * Detiene la reproducción y libera por completo los recursos asociados al servicio
     * (por ejemplo, al abandonar la pantalla de detalle).
     */
    fun liberar()
}
