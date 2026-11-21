package com.perezgomez.recetario.presentation.viewmodels

import androidx.lifecycle.SavedStateHandle
import com.perezgomez.recetario.domain.model.Ingrediente
import com.perezgomez.recetario.domain.model.Receta
import com.perezgomez.recetario.domain.voice.VoiceRecognitionState
import com.perezgomez.recetario.presentation.navigation.Rutas
import com.perezgomez.recetario.presentation.screens.CampoDictado
import com.perezgomez.recetario.util.FakeRecetaRepository
import com.perezgomez.recetario.util.FakeVoiceRecognizer
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
class FormViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var recetaRepository: FakeRecetaRepository
    private lateinit var voiceRecognizer: FakeVoiceRecognizer

    private val recetaExistente = Receta(
        id = "receta-42",
        titulo = "Tacos al Pastor",
        descripcion = "Tradicionales y marinados",
        categoria = "comida",
        tiempoMinutos = 45,
        porciones = 4,
        ingredientes = listOf(Ingrediente("Carne de cerdo", "1", "kg")),
        pasos = listOf("Marinar la carne", "Asar al trompo"),
        favorita = true,
        creadaEn = "2026-11-20T10:00:00Z",
    )

    @Before
    fun setUp() {
        recetaRepository = FakeRecetaRepository()
        voiceRecognizer = FakeVoiceRecognizer()
    }

    @Test
    fun modoCreacion_inicializaEstadoVacioYNoEdicion() {
        val viewModel = FormViewModel(
            recetaRepository = recetaRepository,
            savedStateHandle = SavedStateHandle(),
            voiceRecognizer = voiceRecognizer,
        )

        val state = viewModel.uiState.value
        assertFalse(state.esEdicion)
        assertFalse(state.cargando)
        assertEquals("", state.titulo)
        assertEquals("", state.descripcion)
        assertEquals("1", state.porciones)
    }

    @Test
    fun modoEdicion_precargaDatosDeRecetaExistente() = runTest {
        recetaRepository.recetas.add(recetaExistente)

        val viewModel = FormViewModel(
            recetaRepository = recetaRepository,
            savedStateHandle = SavedStateHandle(mapOf(Rutas.ARG_ID to "receta-42")),
            voiceRecognizer = voiceRecognizer,
        )

        // Estado inicial de edición
        assertTrue(viewModel.uiState.value.esEdicion)

        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.cargando)
        assertEquals("Tacos al Pastor", state.titulo)
        assertEquals("Tradicionales y marinados", state.descripcion)
        assertEquals("comida", state.categoria)
        assertEquals("45", state.tiempoMinutos)
        assertEquals("4", state.porciones)
        assertEquals(1, state.ingredientes.size)
        assertEquals("Carne de cerdo", state.ingredientes[0].nombre)
        assertEquals(2, state.pasos.size)
        assertTrue(state.favorita)
    }

    @Test
    fun onGuardar_conCamposVacios_muestraErroresYNoLlamaRepositorio() = runTest {
        val viewModel = FormViewModel(
            recetaRepository = recetaRepository,
            savedStateHandle = SavedStateHandle(),
            voiceRecognizer = voiceRecognizer,
        )

        viewModel.onGuardar()

        val state = viewModel.uiState.value
        assertFalse(state.guardando)
        assertFalse(state.guardado)
        assertTrue(state.errores.hayErrores())
        assertNotNull(state.errores.titulo)
        assertNotNull(state.errores.categoria)
        assertEquals(0, recetaRepository.llamadasCrear)
    }

    @Test
    fun onGuardar_bloqueaGuardadosDuplicadosMientrasGuarda() = runTest {
        val viewModel = FormViewModel(
            recetaRepository = recetaRepository,
            savedStateHandle = SavedStateHandle(),
            voiceRecognizer = voiceRecognizer,
        )

        // Rellenar datos válidos
        viewModel.onTituloChange("Ensalada César")
        viewModel.onCategoriaChange("comida")
        viewModel.onTiempoChange("20")
        viewModel.onPorcionesChange("2")
        viewModel.onPasoChange(0, "Mezclar lechuga con aderezo")
        val ing = viewModel.uiState.value.ingredientes[0]
        viewModel.onIngredienteChange(0, ing.copy(nombre = "Lechuga romana"))

        // Primer guardado activa guardando = true
        viewModel.onGuardar()
        assertTrue(viewModel.uiState.value.guardando)

        // Segundo guardado concurrente mientras guardando es true
        viewModel.onGuardar()

        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        // Debe haber invocado crear únicamente 1 vez (el segundo guardado fue bloqueado)
        assertEquals(1, recetaRepository.llamadasCrear)
        assertFalse(viewModel.uiState.value.guardando)
        assertTrue(viewModel.uiState.value.guardado)
        assertEquals("Receta creada", viewModel.uiState.value.mensajeExito)
    }

    @Test
    fun onGuardar_enModoEdicion_llamaActualizarYMensajeEsRecetaActualizada() = runTest {
        recetaRepository.recetas.add(recetaExistente)

        val viewModel = FormViewModel(
            recetaRepository = recetaRepository,
            savedStateHandle = SavedStateHandle(mapOf(Rutas.ARG_ID to "receta-42")),
            voiceRecognizer = voiceRecognizer,
        )
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onTituloChange("Tacos al Pastor Especiales")
        viewModel.onGuardar()

        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.guardando)
        assertTrue(state.guardado)
        assertEquals("Receta actualizada", state.mensajeExito)
        assertEquals(1, recetaRepository.llamadasActualizar)
        assertEquals(0, recetaRepository.llamadasCrear)
    }

    @Test
    fun dictadoPorVoz_insertaTextoEnCampoDescripcionActivo() = runTest {
        val viewModel = FormViewModel(
            recetaRepository = recetaRepository,
            savedStateHandle = SavedStateHandle(),
            voiceRecognizer = voiceRecognizer,
        )
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onToggleDictado(CampoDictado.Descripcion)
        assertEquals(CampoDictado.Descripcion, viewModel.uiState.value.campoDictadoActivo)

        // El servicio emite resultado reconocido
        voiceRecognizer.emitirEstado(VoiceRecognitionState.Resultado("Salsa verde picante"))
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Salsa verde picante", state.descripcion)
        assertNull(state.campoDictadoActivo)
    }

    @Test
    fun dictadoPorVoz_concatenaSiYaExistiaTextoPrevio() = runTest {
        val viewModel = FormViewModel(
            recetaRepository = recetaRepository,
            savedStateHandle = SavedStateHandle(),
            voiceRecognizer = voiceRecognizer,
        )
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onDescripcionChange("Receta de la abuela")
        viewModel.onToggleDictado(CampoDictado.Descripcion)

        voiceRecognizer.emitirEstado(VoiceRecognitionState.Resultado("muy fácil de preparar"))
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Receta de la abuela muy fácil de preparar", state.descripcion)
        assertNull(state.campoDictadoActivo)
    }

    @Test
    fun dictadoPorVoz_canceladoNoModificaCamposAnteResultadoTardio() = runTest {
        val viewModel = FormViewModel(
            recetaRepository = recetaRepository,
            savedStateHandle = SavedStateHandle(),
            voiceRecognizer = voiceRecognizer,
        )
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onDescripcionChange("Original")
        viewModel.onToggleDictado(CampoDictado.Descripcion)
        assertEquals(CampoDictado.Descripcion, viewModel.uiState.value.campoDictadoActivo)

        // Usuario cancela el dictado (por ejemplo, navegando atrás o cerrando el diálogo)
        viewModel.onCancelarDictado()
        assertNull(viewModel.uiState.value.campoDictadoActivo)

        // Si llega un resultado asíncrono tardío del engine, no debe alterar el campo
        voiceRecognizer.emitirEstado(VoiceRecognitionState.Resultado("Audio rezagado"))
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("Original", viewModel.uiState.value.descripcion)
    }

    @Test
    fun dictadoPorVoz_cambioDeCampoCancelaSesionPreviaYNoAfectaCampoAnterior() = runTest {
        val viewModel = FormViewModel(
            recetaRepository = recetaRepository,
            savedStateHandle = SavedStateHandle(),
            voiceRecognizer = voiceRecognizer,
        )
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        // 1. Inicia dictado en Descripción
        viewModel.onToggleDictado(CampoDictado.Descripcion)
        voiceRecognizer.emitirEstado(VoiceRecognitionState.Escuchando)
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        // 2. Cambia al Paso 0 mientras grababa
        viewModel.onToggleDictado(CampoDictado.Paso(0))
        assertEquals(CampoDictado.Paso(0), viewModel.uiState.value.campoDictadoActivo)

        // 3. Llega el resultado: debe insertarse en Paso 0 y dejar Descripción vacía
        voiceRecognizer.emitirEstado(VoiceRecognitionState.Resultado("Hornear a 180 grados"))
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("", state.descripcion)
        assertEquals("Hornear a 180 grados", state.pasos[0])
        assertNull(state.campoDictadoActivo)
    }
}
