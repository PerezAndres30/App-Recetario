package com.perezgomez.recetario.presentation.viewmodels

import androidx.lifecycle.SavedStateHandle
import com.perezgomez.recetario.domain.model.Ingrediente
import com.perezgomez.recetario.domain.model.Receta
import com.perezgomez.recetario.domain.model.Usuario
import com.perezgomez.recetario.presentation.navigation.Rutas
import com.perezgomez.recetario.presentation.screens.DetalleContenido
import com.perezgomez.recetario.util.FakeAuthRepository
import com.perezgomez.recetario.util.FakeRecetaRepository
import com.perezgomez.recetario.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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
        descripcion = "Creados por mÃ­",
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
        recetaRepository.recetas.add(recetaGlobal)
        recetaRepository.recetas.add(recetaPropia)
        recetaRepository.registrarRecetaPropia(recetaPropia.id)
    }

    @Test
    fun recetaPropiaVerificada_esPropietarioVerificadoTrue() {
        val viewModel = DetalleViewModel(
            recetaRepository = recetaRepository,
            authRepository = authRepository,
            savedStateHandle = SavedStateHandle(mapOf(Rutas.ARG_ID to "receta-propia-200")),
        )

        assertTrue(viewModel.uiState.value.esPropietarioVerificado)
    }

    @Test
    fun recetaGlobalSinVerificar_esPropietarioVerificadoFalse() {
        val viewModel = DetalleViewModel(
            recetaRepository = recetaRepository,
            authRepository = authRepository,
            savedStateHandle = SavedStateHandle(mapOf(Rutas.ARG_ID to "receta-global-100")),
        )

        assertFalse(viewModel.uiState.value.esPropietarioVerificado)
    }

    @Test
    fun visitante_esPropietarioVerificadoFalse() {
        val authSinSesion = FakeAuthRepository(null)
        val viewModel = DetalleViewModel(
            recetaRepository = recetaRepository,
            authRepository = authSinSesion,
            savedStateHandle = SavedStateHandle(mapOf(Rutas.ARG_ID to "receta-propia-200")),
        )

        assertFalse(viewModel.uiState.value.esPropietarioVerificado)
    }

    @Test
    fun cargar_conExito_emiteContenidoListo() = runTest {
        val viewModel = DetalleViewModel(
            recetaRepository = recetaRepository,
            authRepository = authRepository,
            savedStateHandle = SavedStateHandle(mapOf(Rutas.ARG_ID to "receta-global-100")),
        )

        viewModel.cargar()
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.contenido is DetalleContenido.Listo)
        val contenido = state.contenido as DetalleContenido.Listo
        assertEquals("Mole Poblano Global", contenido.receta.titulo)
    }

    @Test
    fun recetaNoVerificada_bloqueaEliminacion() {
        val viewModel = DetalleViewModel(
            recetaRepository = recetaRepository,
            authRepository = authRepository,
            savedStateHandle = SavedStateHandle(mapOf(Rutas.ARG_ID to "receta-global-100")),
        )

        viewModel.onEliminarClick()
        assertFalse(viewModel.uiState.value.confirmandoEliminar)

        viewModel.onConfirmarEliminar()
        assertEquals(0, recetaRepository.llamadasEliminar)
    }

    @Test
    fun recetaPropiaVerificada_permiteEliminar() = runTest {
        val viewModel = DetalleViewModel(
            recetaRepository = recetaRepository,
            authRepository = authRepository,
            savedStateHandle = SavedStateHandle(mapOf(Rutas.ARG_ID to "receta-propia-200")),
        )

        viewModel.cargar()
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onEliminarClick()
        assertTrue(viewModel.uiState.value.confirmandoEliminar)

        viewModel.onConfirmarEliminar()
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(1, recetaRepository.llamadasEliminar)
        assertTrue(viewModel.uiState.value.eliminada)
    }
}
