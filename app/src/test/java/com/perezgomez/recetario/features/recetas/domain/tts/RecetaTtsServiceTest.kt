package com.perezgomez.recetario.features.recetas.domain.tts

import com.perezgomez.recetario.features.recetas.domain.entities.Ingrediente
import com.perezgomez.recetario.features.recetas.domain.entities.Receta
import com.perezgomez.recetario.util.FakeTtsEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class RecetaTtsServiceTest {

    private lateinit var fakeTtsEngine: FakeTtsEngine
    private lateinit var ttsService: RecetaTtsServiceImpl

    private val recetaPrueba = Receta(
        id = "receta-1",
        titulo = "Tacos de Guacamole",
        descripcion = "Deliciosos tacos tradicionales mexicanos",
        categoria = "comida",
        tiempoMinutos = 20,
        porciones = 4,
        ingredientes = listOf(
            Ingrediente(nombre = "Aguacates", cantidad = "3", unidad = "piezas"),
            Ingrediente(nombre = "Cilantro", cantidad = "1", unidad = "manojo"),
            Ingrediente(nombre = "Sal", cantidad = "al gusto", unidad = ""),
        ),
        pasos = listOf(
            "Triturar los aguacates en un tazón hondo.",
            "Picar finamente el cilantro e incorporarlo.",
            "Agregar sal al gusto y servir con tortillas calientes.",
        ),
    )

    @Before
    fun setUp() {
        fakeTtsEngine = FakeTtsEngine()
        ttsService = RecetaTtsServiceImpl(fakeTtsEngine)
    }

    @Test
    fun estadoInicial_estaInactivoYSinReproduccion() {
        val estado = ttsService.estado.value

        assertEquals(EtapaLectura.INACTIVO, estado.etapa)
        assertEquals(0, estado.pasoActual)
        assertEquals(0, estado.totalPasos)
        assertFalse(estado.estaReproduciendo)
        assertEquals("", estado.textoActual)
        assertNull(estado.error)
        assertFalse(estado.estaActivo)
        assertFalse(estado.puedeContinuar)
        assertFalse(estado.puedeRepetir)
    }

    @Test
    fun reproducir_iniciaEnIntroduccionConTituloDescripcionEIngredientes() {
        ttsService.reproducir(recetaPrueba)

        val estado = ttsService.estado.value
        assertEquals(EtapaLectura.INTRODUCCION, estado.etapa)
        assertEquals(0, estado.pasoActual)
        assertEquals(3, estado.totalPasos)
        assertTrue(estado.estaReproduciendo)
        assertTrue(estado.estaActivo)
        assertTrue(estado.puedeContinuar)
        assertTrue(estado.puedeRepetir)
        assertFalse(estado.esFinalizado)

        // Verificar que el texto contiene el título, descripción e ingredientes
        assertTrue(estado.textoActual.contains("Tacos de Guacamole"))
        assertTrue(estado.textoActual.contains("Deliciosos tacos tradicionales mexicanos"))
        assertTrue(estado.textoActual.contains("3 piezas de Aguacates"))
        assertTrue(estado.textoActual.contains("1 manojo de Cilantro"))
        assertTrue(estado.textoActual.contains("al gusto de Sal") || estado.textoActual.contains("Sal"))

        // Verificar llamada al motor
        assertEquals(1, fakeTtsEngine.textosHablados.size)
        assertEquals(estado.textoActual, fakeTtsEngine.textosHablados.first())
        assertEquals("intro", fakeTtsEngine.utteranceIds.first())
    }

    @Test
    fun continuar_desdeIntroduccion_avanzaAPaso1YLeeInstruccion() {
        ttsService.reproducir(recetaPrueba)

        ttsService.continuar()

        val estado = ttsService.estado.value
        assertEquals(EtapaLectura.PASO, estado.etapa)
        assertEquals(1, estado.pasoActual)
        assertEquals(3, estado.totalPasos)
        assertTrue(estado.estaReproduciendo)
        assertTrue(estado.puedeContinuar)
        assertTrue(estado.puedeRepetir)

        assertEquals("Paso 1 de 3: Triturar los aguacates en un tazón hondo.", estado.textoActual)
        assertEquals("paso-1", fakeTtsEngine.utteranceIds.last())
    }

    @Test
    fun continuar_aTravesDeTodosLosPasos_avanzaSecuencialmente() {
        ttsService.reproducir(recetaPrueba)

        // Intro -> Paso 1
        ttsService.continuar()
        assertEquals(1, ttsService.estado.value.pasoActual)
        assertEquals("Paso 1 de 3: Triturar los aguacates en un tazón hondo.", ttsService.estado.value.textoActual)

        // Paso 1 -> Paso 2
        ttsService.continuar()
        assertEquals(2, ttsService.estado.value.pasoActual)
        assertEquals("Paso 2 de 3: Picar finamente el cilantro e incorporarlo.", ttsService.estado.value.textoActual)
        assertEquals("paso-2", fakeTtsEngine.utteranceIds.last())

        // Paso 2 -> Paso 3
        ttsService.continuar()
        assertEquals(3, ttsService.estado.value.pasoActual)
        assertEquals("Paso 3 de 3: Agregar sal al gusto y servir con tortillas calientes.", ttsService.estado.value.textoActual)
        assertEquals("paso-3", fakeTtsEngine.utteranceIds.last())
    }

    @Test
    fun continuar_despuesDelUltimoPaso_anunciaFinalizacion() {
        ttsService.reproducir(recetaPrueba)
        ttsService.continuar() // Paso 1
        ttsService.continuar() // Paso 2
        ttsService.continuar() // Paso 3

        // Desde el último paso, continuar debe finalizar
        ttsService.continuar()

        val estado = ttsService.estado.value
        assertEquals(EtapaLectura.FINALIZADO, estado.etapa)
        assertTrue(estado.esFinalizado)
        assertFalse(estado.puedeContinuar)
        assertFalse(estado.puedeRepetir)
        assertEquals(RecetaTtsServiceImpl.MENSAJE_FINALIZACION, estado.textoActual)
        assertEquals("fin", fakeTtsEngine.utteranceIds.last())
    }

    @Test
    fun regresion_ultimoPaso_permiteContinuarHaciaFinalizado_yBloqueaContinuarPosterior() {
        ttsService.reproducir(recetaPrueba)
        ttsService.continuar() // Paso 1
        ttsService.continuar() // Paso 2
        ttsService.continuar() // Paso 3 (último paso de la receta: totalPasos = 3)

        val estadoEnUltimoPaso = ttsService.estado.value
        assertEquals(3, estadoEnUltimoPaso.pasoActual)
        assertEquals(3, estadoEnUltimoPaso.totalPasos)
        assertEquals(EtapaLectura.PASO, estadoEnUltimoPaso.etapa)
        // 1. Comprobar que en el último paso puede pulsarse Continuar para llegar a FINALIZADO
        assertTrue(
            "Estando en el último paso se debe poder pulsar Continuar para finalizar la receta",
            estadoEnUltimoPaso.puedeContinuar,
        )

        // 2. Pulsar Continuar desde el último paso
        ttsService.continuar()

        val estadoFinalizado = ttsService.estado.value
        assertEquals(EtapaLectura.FINALIZADO, estadoFinalizado.etapa)
        assertTrue(estadoFinalizado.esFinalizado)
        // 3. Comprobar que no se pueda continuar nuevamente desde dicho estado
        assertFalse(
            "Desde el estado FINALIZADO no debe permitirse continuar nuevamente",
            estadoFinalizado.puedeContinuar,
        )
        // Total de solicitudes de locución hasta la finalización: intro + paso 1 + paso 2 + paso 3 + fin = 5
        val cantidadLocucionesAlFinalizar = fakeTtsEngine.textosHablados.size
        assertEquals(5, cantidadLocucionesAlFinalizar)
        assertEquals("fin", fakeTtsEngine.utteranceIds.last())

        // 4. Intentos posteriores de continuar() se ignoran por completo: no generan locuciones ni modifican estado
        ttsService.continuar()
        ttsService.continuar()

        val estadoDespuesDeIntentarContinuar = ttsService.estado.value
        assertEquals(EtapaLectura.FINALIZADO, estadoDespuesDeIntentarContinuar.etapa)
        assertFalse(estadoDespuesDeIntentarContinuar.puedeContinuar)
        assertEquals(
            "No deben generarse nuevas solicitudes de locución tras haber finalizado",
            cantidadLocucionesAlFinalizar,
            fakeTtsEngine.textosHablados.size,
        )
    }

    @Test
    fun repetir_enIntroduccion_vuelveALeerLaIntroduccion() {
        ttsService.reproducir(recetaPrueba)
        val textoIntro = ttsService.estado.value.textoActual

        ttsService.repetir()

        val estado = ttsService.estado.value
        assertEquals(EtapaLectura.INTRODUCCION, estado.etapa)
        assertEquals(0, estado.pasoActual)
        assertEquals(textoIntro, estado.textoActual)
        assertEquals(2, fakeTtsEngine.textosHablados.size)
        assertEquals(textoIntro, fakeTtsEngine.textosHablados.last())
    }

    @Test
    fun repetir_enPaso_vuelveALeerElPasoActual() {
        ttsService.reproducir(recetaPrueba)
        ttsService.continuar() // Paso 1
        ttsService.continuar() // Paso 2

        ttsService.repetir()

        val estado = ttsService.estado.value
        assertEquals(EtapaLectura.PASO, estado.etapa)
        assertEquals(2, estado.pasoActual)
        assertEquals("Paso 2 de 3: Picar finamente el cilantro e incorporarlo.", estado.textoActual)
        assertEquals("paso-2", fakeTtsEngine.utteranceIds.last())
    }

    @Test
    fun repetir_enFinalizado_vuelveALeerMensajeDeFinalizacion() {
        ttsService.reproducir(recetaPrueba)
        ttsService.continuar()
        ttsService.continuar()
        ttsService.continuar()
        ttsService.continuar() // Finalizado

        val llamadasPrevias = fakeTtsEngine.textosHablados.size
        ttsService.repetir()

        assertEquals(llamadasPrevias + 1, fakeTtsEngine.textosHablados.size)
        assertEquals(RecetaTtsServiceImpl.MENSAJE_FINALIZACION, fakeTtsEngine.textosHablados.last())
        assertEquals(EtapaLectura.FINALIZADO, ttsService.estado.value.etapa)
    }

    @Test
    fun reproducir_desdeFinalizado_permiteReiniciarLaLecturaDesdeInicio() {
        ttsService.reproducir(recetaPrueba)
        ttsService.continuar()
        ttsService.continuar()
        ttsService.continuar()
        ttsService.continuar() // Finalizado
        assertTrue(ttsService.estado.value.esFinalizado)

        // Reinicio desde el inicio
        ttsService.reproducir(recetaPrueba)

        val estado = ttsService.estado.value
        assertEquals(EtapaLectura.INTRODUCCION, estado.etapa)
        assertEquals(0, estado.pasoActual)
        assertTrue(estado.textoActual.contains("Tacos de Guacamole"))
        assertFalse(estado.esFinalizado)
    }

    @Test
    fun detener_pausaLaLocucionYSensibilizaEstadoSinPerderProgreso() {
        ttsService.reproducir(recetaPrueba)
        ttsService.continuar() // Paso 1
        assertEquals(1, ttsService.estado.value.pasoActual)
        assertTrue(ttsService.estado.value.estaReproduciendo)

        ttsService.detener()

        val estado = ttsService.estado.value
        assertFalse(estado.estaReproduciendo)
        assertEquals(EtapaLectura.PASO, estado.etapa)
        assertEquals(1, estado.pasoActual)
        assertEquals(1, fakeTtsEngine.llamadasDetener)
    }

    @Test
    fun liberar_detieneYReiniciaEstadoPorCompleto() {
        ttsService.reproducir(recetaPrueba)
        ttsService.continuar()

        ttsService.liberar()

        val estado = ttsService.estado.value
        assertEquals(EtapaLectura.INACTIVO, estado.etapa)
        assertEquals(0, estado.pasoActual)
        assertEquals(0, estado.totalPasos)
        assertFalse(estado.estaReproduciendo)
        assertEquals("", estado.textoActual)
        assertEquals(1, fakeTtsEngine.llamadasLiberar)

        // Continuar tras liberar no produce efectos
        ttsService.continuar()
        assertEquals(EtapaLectura.INACTIVO, ttsService.estado.value.etapa)
    }

    @Test
    fun recetaSinPasos_avanzaDirectamenteAFinalizado() {
        val recetaSinPasos = recetaPrueba.copy(pasos = emptyList())
        ttsService.reproducir(recetaSinPasos)

        assertEquals(EtapaLectura.INTRODUCCION, ttsService.estado.value.etapa)
        assertEquals(0, ttsService.estado.value.totalPasos)

        ttsService.continuar()

        assertEquals(EtapaLectura.FINALIZADO, ttsService.estado.value.etapa)
        assertEquals(RecetaTtsServiceImpl.MENSAJE_FINALIZACION, ttsService.estado.value.textoActual)
    }

    @Test
    fun eventosDeProgreso_onInicioYFin_actualizanEstaReproduciendo() {
        ttsService.reproducir(recetaPrueba)
        assertTrue(ttsService.estado.value.estaReproduciendo)

        // Simular que el motor termina de hablar
        fakeTtsEngine.simularFinLocucion()
        assertFalse(ttsService.estado.value.estaReproduciendo)

        // Avanzar vuelve a encender estaReproduciendo
        ttsService.continuar()
        assertTrue(ttsService.estado.value.estaReproduciendo)
    }

    @Test
    fun errorDeSintesis_actualizaMensajeDeError() {
        ttsService.reproducir(recetaPrueba)

        fakeTtsEngine.simularError("Falla de síntesis de audio")

        val estado = ttsService.estado.value
        assertFalse(estado.estaReproduciendo)
        assertNotNull(estado.error)
        assertEquals("Falla de síntesis de audio", estado.error)
    }
}
