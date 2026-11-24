package com.perezgomez.recetario.domain.tts

import com.perezgomez.recetario.domain.model.Ingrediente
import com.perezgomez.recetario.domain.model.Receta
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecetaTtsServiceImpl @Inject constructor(
    private val ttsEngine: TtsEngine,
) : RecetaTtsService, TtsEventListener {

    private val _estado = MutableStateFlow(EstadoLectura())
    override val estado: StateFlow<EstadoLectura> = _estado.asStateFlow()

    private var recetaActual: Receta? = null

    init {
        ttsEngine.setListener(this)
    }

    override fun reproducir(receta: Receta) {
        recetaActual = receta
        val total = receta.pasos.size
        val textoIntro = construirTextoIntroduccion(receta)

        _estado.value = EstadoLectura(
            etapa = EtapaLectura.INTRODUCCION,
            pasoActual = 0,
            totalPasos = total,
            estaReproduciendo = true,
            textoActual = textoIntro,
            error = null,
        )

        ttsEngine.hablar(textoIntro, utteranceId = "intro")
    }

    override fun continuar() {
        val receta = recetaActual ?: return
        val estadoActual = _estado.value

        when (estadoActual.etapa) {
            EtapaLectura.INTRODUCCION -> {
                if (receta.pasos.isNotEmpty()) {
                    val siguientePaso = 1
                    val textoPaso = construirTextoPaso(siguientePaso, receta.pasos.size, receta.pasos[0])

                    _estado.value = estadoActual.copy(
                        etapa = EtapaLectura.PASO,
                        pasoActual = siguientePaso,
                        estaReproduciendo = true,
                        textoActual = textoPaso,
                        error = null,
                    )
                    ttsEngine.hablar(textoPaso, utteranceId = "paso-$siguientePaso")
                } else {
                    finalizarLectura(estadoActual)
                }
            }
            EtapaLectura.PASO -> {
                if (estadoActual.pasoActual < receta.pasos.size) {
                    val siguientePaso = estadoActual.pasoActual + 1
                    val textoPaso = construirTextoPaso(
                        siguientePaso,
                        receta.pasos.size,
                        receta.pasos[siguientePaso - 1],
                    )

                    _estado.value = estadoActual.copy(
                        etapa = EtapaLectura.PASO,
                        pasoActual = siguientePaso,
                        estaReproduciendo = true,
                        textoActual = textoPaso,
                        error = null,
                    )
                    ttsEngine.hablar(textoPaso, utteranceId = "paso-$siguientePaso")
                } else {
                    finalizarLectura(estadoActual)
                }
            }
            EtapaLectura.FINALIZADO -> {
                // Ya ha finalizado; cualquier llamada a continuar() se ignora sin generar locuciones ni modificar estado
            }
            EtapaLectura.INACTIVO -> {
                // Sin receta en reproducción
            }
        }
    }

    override fun repetir() {
        val receta = recetaActual ?: return
        val estadoActual = _estado.value

        when (estadoActual.etapa) {
            EtapaLectura.INTRODUCCION -> {
                val texto = construirTextoIntroduccion(receta)
                _estado.value = estadoActual.copy(
                    estaReproduciendo = true,
                    textoActual = texto,
                    error = null,
                )
                ttsEngine.hablar(texto, utteranceId = "intro")
            }
            EtapaLectura.PASO -> {
                val indice = estadoActual.pasoActual - 1
                if (indice in receta.pasos.indices) {
                    val texto = construirTextoPaso(
                        estadoActual.pasoActual,
                        receta.pasos.size,
                        receta.pasos[indice],
                    )
                    _estado.value = estadoActual.copy(
                        estaReproduciendo = true,
                        textoActual = texto,
                        error = null,
                    )
                    ttsEngine.hablar(texto, utteranceId = "paso-${estadoActual.pasoActual}")
                }
            }
            EtapaLectura.FINALIZADO -> {
                _estado.value = estadoActual.copy(
                    estaReproduciendo = true,
                    textoActual = MENSAJE_FINALIZACION,
                    error = null,
                )
                ttsEngine.hablar(MENSAJE_FINALIZACION, utteranceId = "fin")
            }
            EtapaLectura.INACTIVO -> {
                // Sin contenido activo
            }
        }
    }

    override fun detener() {
        ttsEngine.detener()
        _estado.update { it.copy(estaReproduciendo = false) }
    }

    override fun liberar() {
        ttsEngine.liberar()
        recetaActual = null
        _estado.value = EstadoLectura(
            etapa = EtapaLectura.INACTIVO,
            pasoActual = 0,
            totalPasos = 0,
            estaReproduciendo = false,
            textoActual = "",
            error = null,
        )
    }

    override fun onInicio() {
        _estado.update { it.copy(estaReproduciendo = true) }
    }

    override fun onFin() {
        _estado.update { it.copy(estaReproduciendo = false) }
    }

    override fun onError(mensaje: String) {
        _estado.update { it.copy(estaReproduciendo = false, error = mensaje) }
    }

    private fun finalizarLectura(estadoActual: EstadoLectura) {
        _estado.value = estadoActual.copy(
            etapa = EtapaLectura.FINALIZADO,
            estaReproduciendo = true,
            textoActual = MENSAJE_FINALIZACION,
            error = null,
        )
        ttsEngine.hablar(MENSAJE_FINALIZACION, utteranceId = "fin")
    }

    companion object {
        const val MENSAJE_FINALIZACION =
            "Has completado la receta. ¡Buen provecho! Puedes reproducirla de nuevo desde el inicio."

        fun construirTextoIntroduccion(receta: Receta): String {
            val builder = StringBuilder()
            builder.append("Receta: ").append(receta.titulo.trim()).append(". ")
            if (receta.descripcion.isNotBlank()) {
                builder.append(receta.descripcion.trim()).append(". ")
            }
            if (receta.ingredientes.isNotEmpty()) {
                builder.append("Ingredientes: ")
                val listaFormateada = receta.ingredientes.map { formatearIngrediente(it) }
                builder.append(listaFormateada.joinToString(", ")).append(".")
            }
            return builder.toString().trim()
        }

        fun construirTextoPaso(pasoNumero: Int, totalPasos: Int, textoPaso: String): String {
            return "Paso $pasoNumero de $totalPasos: ${textoPaso.trim()}"
        }

        fun formatearIngrediente(ingrediente: Ingrediente): String {
            val cantidadYUnidad = listOf(ingrediente.cantidad, ingrediente.unidad)
                .filter { it.isNotBlank() }
                .joinToString(" ")
            return if (cantidadYUnidad.isNotBlank()) {
                "$cantidadYUnidad de ${ingrediente.nombre}".trim()
            } else {
                ingrediente.nombre.trim()
            }
        }
    }
}
