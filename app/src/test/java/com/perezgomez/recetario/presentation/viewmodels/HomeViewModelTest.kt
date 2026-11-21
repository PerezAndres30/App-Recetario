package com.perezgomez.recetario.presentation.viewmodels

import com.perezgomez.recetario.domain.model.Receta
import com.perezgomez.recetario.domain.model.Usuario
import com.perezgomez.recetario.presentation.screens.ListaUi
import com.perezgomez.recetario.util.FakeAuthRepository
import com.perezgomez.recetario.util.FakeRecetaRepository
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
class HomeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var recetaRepository: FakeRecetaRepository
    private lateinit var authRepository: FakeAuthRepository
    private lateinit var viewModel: HomeViewModel

    private val recetaPrueba = Receta(
        id = "receta-1",
        titulo = "Guacamole",
        descripcion = "Tradicional mexicano",
        categoria = "comida",
        tiempoMinutos = 15,
        porciones = 4,
        ingredientes = emptyList(),
        pasos = emptyList(),
    )

    @Before
    fun setUp() {
        recetaRepository = FakeRecetaRepository()
        authRepository = FakeAuthRepository(Usuario("user-1", "Carlos", "carlos@test.com"))
        viewModel = HomeViewModel(recetaRepository, authRepository)
    }

    @Test
    fun estadoInicial_muestraUsuarioYListaCargando() {
        val state = viewModel.uiState.value
        assertEquals("Carlos", state.nombreUsuario)
        assertTrue(state.lista is ListaUi.Cargando)
        assertFalse(state.actualizando)
    }

    @Test
    fun cargar_conListaVacia_actualizandoVuelveAFalseYListaEsVacia() = runTest {
        viewModel.cargar()

        // Antes de que avance la coroutine, actualizando es true
        assertTrue(viewModel.uiState.value.actualizando)

        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.actualizando)
        assertTrue(state.lista is ListaUi.Vacia)
    }

    @Test
    fun cargar_conRecetas_actualizandoVuelveAFalseYListaEsContenido() = runTest {
        recetaRepository.recetas.add(recetaPrueba)

        viewModel.cargar()
        assertTrue(viewModel.uiState.value.actualizando)

        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.actualizando)
        assertTrue(state.lista is ListaUi.Contenido)
        val contenido = state.lista as ListaUi.Contenido
        assertEquals(1, contenido.recetas.size)
        assertEquals("Guacamole", contenido.recetas[0].titulo)
    }

    @Test
    fun cargar_conFalloEnCargaInicial_actualizandoVuelveAFalseYListaEsError() = runTest {
        recetaRepository.deberiaFallar = true
        recetaRepository.mensajeError = "Fallo de conexión"

        viewModel.cargar()
        assertTrue(viewModel.uiState.value.actualizando)

        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.actualizando)
        assertTrue(state.lista is ListaUi.Error)
        val error = state.lista as ListaUi.Error
        assertEquals("Fallo de conexión", error.mensaje)
    }

    @Test
    fun cargar_enSegundoPlanoCuandoYaHayContenido_conservaListaYActualizandoVuelveAFalse() = runTest {
        // 1. Carga inicial con éxito
        recetaRepository.recetas.add(recetaPrueba)
        viewModel.cargar()
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()
        assertTrue(viewModel.uiState.value.lista is ListaUi.Contenido)

        // 2. Nueva actualización en segundo plano
        viewModel.cargar()
        // La lista no debe reiniciarse a Cargando mientras se actualiza en segundo plano
        assertTrue(viewModel.uiState.value.lista is ListaUi.Contenido)
        assertTrue(viewModel.uiState.value.actualizando)

        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        // 3. Al finalizar, actualizando vuelve a false
        assertFalse(viewModel.uiState.value.actualizando)
        assertTrue(viewModel.uiState.value.lista is ListaUi.Contenido)
    }

    @Test
    fun cargar_enSegundoPlanoConError_conservaListaYNotificaMensaje() = runTest {
        // Carga inicial
        recetaRepository.recetas.add(recetaPrueba)
        viewModel.cargar()
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        // Error en actualización posterior
        recetaRepository.deberiaFallar = true
        recetaRepository.mensajeError = "Error de red"
        viewModel.cargar()
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.actualizando)
        assertTrue(state.lista is ListaUi.Contenido)
        assertEquals("Error de red", state.mensaje)
    }

    @Test
    fun prevencionDeEliminacionesDuplicadas_bloqueaLlamadasConcurrentes() = runTest {
        recetaRepository.recetas.add(recetaPrueba)
        viewModel.cargar()
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onEliminarClick(recetaPrueba)
        assertEquals(recetaPrueba, viewModel.uiState.value.recetaAEliminar)

        // Primera confirmación inicia la eliminación
        viewModel.onConfirmarEliminar()
        assertTrue(viewModel.uiState.value.eliminando)

        // Segunda llamada concurrente mientras eliminando es true
        viewModel.onConfirmarEliminar()

        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        // Verificamos que solo se haya emitido 1 llamada al repositorio (la segunda fue prevenida)
        assertEquals(1, recetaRepository.llamadasEliminar)
        assertFalse(viewModel.uiState.value.eliminando)
        assertNull(viewModel.uiState.value.recetaAEliminar)
        assertEquals("Receta eliminada", viewModel.uiState.value.mensaje)
    }
}
