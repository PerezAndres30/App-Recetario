package com.perezgomez.recetario.presentation.viewmodels

import androidx.lifecycle.SavedStateHandle
import com.perezgomez.recetario.domain.model.Ingrediente
import com.perezgomez.recetario.domain.model.Receta
import com.perezgomez.recetario.domain.model.Usuario
import com.perezgomez.recetario.domain.tts.EtapaLectura
import com.perezgomez.recetario.presentation.navigation.Rutas
import com.perezgomez.recetario.presentation.screens.DetalleContenido
import com.perezgomez.recetario.util.FakeAuthRepository
import com.perezgomez.recetario.util.FakeRecetaRepository
import com.perezgomez.recetario.util.FakeRecetaTtsService
import com.perezgomez.recetario.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DetalleViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var recetaRepository: FakeRecetaRepository
    private lateinit var authRepository: FakeAuthRepository
    private lateinit var fakeTtsService: FakeRecetaTtsService

    private val recetaGlobal = Receta(
        id = "receta-global-100",
        titulo = "Mole Poblano Global",
        descripcion = "Tradicional de la comunidad",
        categoria = "comida",
        tiempoMinutos = 90,
        porciones = 6,
        ingredientes = listOf(Ingrediente("Pollo", "1", "kg")),
        pasos = listOf("Cocer el pollo", "Preparar la pasta de mole"),
    )

    private val recetaPropia = Receta(
        id = "receta-propia-200",
        titulo = "Tacos Propios",
        descripcion = "Creados por mí",
        categoria = "comida",
        tiempoMinutos = 20,
        porciones = 2,
        ingredientes = emptyList(),
        pasos = emptyList(),
    )

    @Before
    fun setUp() {
        recetaRepository = FakeRecetaRepository()
        authRepository = FakeAuthRepository(Usuario("user-1", "Chef Ivan", "ivan@test.com"))
        fakeTtsService = FakeRecetaTtsService()
        recetaRepository.recetas.add(recetaGlobal)
        recetaRepository.recetas.add(recetaPropia)
        recetaRepository.registrarRecetaPropia(recetaPropia.id)
    }

    private fun crearViewModel(
        recetaId: String,
        auth: FakeAuthRepository = authRepository,
    ) = DetalleViewModel(
        recetaRepository = recetaRepository,
        authRepository = auth,
        recetaTtsService = fakeTtsService,
        savedStateHandle = SavedStateHandle(mapOf(Rutas.ARG_ID to recetaId)),
    )

    @Test
    fun recetaPropiaVerificada_esPropietarioVerificadoTrue() {
        val viewModel = crearViewModel("receta-propia-200")

        assertTrue(viewModel.uiState.value.esPropietarioVerificado)
    }

    @Test
    fun recetaGlobalSinVerificar_esPropietarioVerificadoFalse() {
        val viewModel = crearViewModel("receta-global-100")

        assertFalse(viewModel.uiState.value.esPropietarioVerificado)
    }

    @Test
    fun visitante_esPropietarioVerificadoFalse() {
        val authSinSesion = FakeAuthRepository(null)
        val viewModel = crearViewModel("receta-propia-200", auth = authSinSesion)

        assertFalse(viewModel.uiState.value.esPropietarioVerificado)
    }

    @Test
    fun cargar_conExito_emiteContenidoListo() = runTest {
        val viewModel = crearViewModel("receta-global-100")

        viewModel.cargar()
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.contenido is DetalleContenido.Listo)
        val contenido = state.contenido as DetalleContenido.Listo
        assertEquals("Mole Poblano Global", contenido.receta.titulo)
    }

    @Test
    fun recetaNoVerificada_bloqueaEliminacion() {
        val viewModel = crearViewModel("receta-global-100")

        viewModel.onEliminarClick()
        assertFalse(viewModel.uiState.value.confirmandoEliminar)

        viewModel.onConfirmarEliminar()
        assertEquals(0, recetaRepository.llamadasEliminar)
    }

    @Test
    fun recetaPropiaVerificada_permiteEliminar() = runTest {
        val viewModel = crearViewModel("receta-propia-200")

        viewModel.cargar()
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onEliminarClick()
        assertTrue(viewModel.uiState.value.confirmandoEliminar)

        viewModel.onConfirmarEliminar()
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(1, recetaRepository.llamadasEliminar)
        assertTrue(viewModel.uiState.value.eliminada)
    }

    // ================= PRUEBAS DE TEXT-TO-SPEECH (ETAPA 4) =================

    @Test
    fun onReproducirTts_conRecetaCargada_iniciaLecturaEnTtsService() = runTest {
        val viewModel = crearViewModel("receta-global-100")
        viewModel.cargar()
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onReproducirTts()
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("receta-global-100", fakeTtsService.recetaReproducida?.id)
        assertEquals(EtapaLectura.INTRODUCCION, viewModel.uiState.value.ttsState.etapa)
        assertEquals(0, viewModel.uiState.value.ttsState.pasoActual)
        assertTrue(viewModel.uiState.value.ttsState.estaReproduciendo)
    }

    @Test
    fun onContinuarTts_avanzaPasoEnTtsService() = runTest {
        val viewModel = crearViewModel("receta-global-100")
        viewModel.cargar()
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onReproducirTts()
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        // Avanzar a paso 1
        viewModel.onContinuarTts()
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(1, fakeTtsService.llamadasContinuar)
        assertEquals(EtapaLectura.PASO, viewModel.uiState.value.ttsState.etapa)
        assertEquals(1, viewModel.uiState.value.ttsState.pasoActual)
    }

    @Test
    fun onRepetirTts_invocaRepetirEnTtsService() = runTest {
        val viewModel = crearViewModel("receta-global-100")
        viewModel.cargar()
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onRepetirTts()
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(1, fakeTtsService.llamadasRepetir)
    }

    @Test
    fun visitante_puedeReproducirYEscucharRecetasSinAutenticacion() = runTest {
        val authSinSesion = FakeAuthRepository(null)
        val viewModel = crearViewModel("receta-global-100", auth = authSinSesion)
        viewModel.cargar()
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        // El visitante debe poder invocar Reproducir sin requerir login
        viewModel.onReproducirTts()
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("receta-global-100", fakeTtsService.recetaReproducida?.id)
        assertTrue(viewModel.uiState.value.ttsState.estaActivo)

        // El visitante puede avanzar con Continuar
        viewModel.onContinuarTts()
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(1, fakeTtsService.llamadasContinuar)
        assertEquals(1, viewModel.uiState.value.ttsState.pasoActual)
    }

    @Test
    fun detenerLectura_invocaDetenerEnTtsServiceYDetieneLocucion() = runTest {
        val viewModel = crearViewModel("receta-global-100")
        viewModel.cargar()
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onReproducirTts()
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()
        assertTrue(viewModel.uiState.value.ttsState.estaReproduciendo)

        viewModel.detenerLectura()
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(1, fakeTtsService.llamadasDetener)
        assertFalse(viewModel.uiState.value.ttsState.estaReproduciendo)
    }

    @Test
    fun eliminacionExitosa_detieneLecturaEnTtsService() = runTest {
        val viewModel = crearViewModel("receta-propia-200")
        viewModel.cargar()
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onReproducirTts()
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onConfirmarEliminar()
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(fakeTtsService.llamadasDetener >= 1)
        assertTrue(viewModel.uiState.value.eliminada)
    }

    @Test
    fun regresion_ultimoPaso_onContinuarTts_alcanzaFinalizado_yBloqueaContinuarPosterior() = runTest {
        val viewModel = crearViewModel("receta-global-100")
        viewModel.cargar()
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onReproducirTts()
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        // Avanzar a Paso 1
        viewModel.onContinuarTts()
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(1, viewModel.uiState.value.ttsState.pasoActual)

        // Avanzar a Paso 2 (último paso de recetaGlobal)
        viewModel.onContinuarTts()
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(2, viewModel.uiState.value.ttsState.pasoActual)
        assertEquals(2, viewModel.uiState.value.ttsState.totalPasos)
        assertTrue(viewModel.uiState.value.ttsState.puedeContinuar)

        // Pulsar Continuar desde el último paso debe alcanzar FINALIZADO
        viewModel.onContinuarTts()
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(EtapaLectura.FINALIZADO, viewModel.uiState.value.ttsState.etapa)
        assertTrue(viewModel.uiState.value.ttsState.esFinalizado)
        assertFalse(viewModel.uiState.value.ttsState.puedeContinuar)
    }
}
