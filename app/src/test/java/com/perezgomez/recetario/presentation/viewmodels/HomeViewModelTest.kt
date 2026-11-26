package com.perezgomez.recetario.presentation.viewmodels

import com.perezgomez.recetario.domain.model.Receta
import com.perezgomez.recetario.domain.model.Usuario
import com.perezgomez.recetario.presentation.screens.ListaUi
import com.perezgomez.recetario.presentation.screens.ModoHome
import com.perezgomez.recetario.util.FakeAuthRepository
import com.perezgomez.recetario.util.FakeRecetaRepository
import com.perezgomez.recetario.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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

    private val recetaGlobal = Receta(
        id = "receta-comunitaria-1",
        titulo = "Guacamole Comunitario",
        descripcion = "Tradicional de la comunidad",
        categoria = "comida",
        tiempoMinutos = 15,
        porciones = 4,
        ingredientes = emptyList(),
        pasos = emptyList(),
    )

    private val recetaPropia = Receta(
        id = "receta-propia-1",
        titulo = "Ensalada del Chef",
        descripcion = "Creada por el usuario actual",
        categoria = "comida",
        tiempoMinutos = 10,
        porciones = 2,
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
    fun estadoInicial_conSesion_modoMisRecetasYAutenticado() {
        val state = viewModel.uiState.value
        assertEquals("Carlos", state.nombreUsuario)
        assertTrue(state.estaAutenticado)
        assertEquals(ModoHome.MIS_RECETAS, state.modo)
    }

    @Test
    fun estadoInicial_sinSesion_modoExplorar() {
        val authSinSesion = FakeAuthRepository(null)
        val vmVisitante = HomeViewModel(recetaRepository, authSinSesion)

        val state = vmVisitante.uiState.value
        assertEquals("", state.nombreUsuario)
        assertFalse(state.estaAutenticado)
        assertEquals(ModoHome.EXPLORAR, state.modo)
    }

    @Test
    fun modoExplorar_muestraCatalogoGlobalCompleto() = runTest {
        recetaRepository.recetas.add(recetaGlobal)
        viewModel.configurarModo(ModoHome.EXPLORAR)
        viewModel.cargar()

        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.lista is ListaUi.Contenido)
        val contenido = state.lista as ListaUi.Contenido
        assertEquals(1, contenido.recetas.size)
        assertEquals("Guacamole Comunitario", contenido.recetas[0].titulo)
    }

    @Test
    fun modoMisRecetas_sinRecetasPropias_noMuestraRecetasComunitariasYListaEsVacia() = runTest {
        // En la base de datos hay recetas comunitarias
        recetaRepository.recetas.add(recetaGlobal)
        // Pero ninguna ha sido verificada como propia del usuario
        viewModel.configurarModo(ModoHome.MIS_RECETAS)
        viewModel.cargar()

        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        // Mis recetas no debe mostrar la receta global como si fuera propia
        val state = viewModel.uiState.value
        assertTrue(state.lista is ListaUi.Vacia)
    }

    @Test
    fun modoMisRecetas_conRecetaPropiaVerificada_muestraUnicamenteLaRecetaPropia() = runTest {
        recetaRepository.recetas.add(recetaGlobal)
        recetaRepository.recetas.add(recetaPropia)
        recetaRepository.registrarRecetaPropia(recetaPropia.id)

        viewModel.configurarModo(ModoHome.MIS_RECETAS)
        viewModel.cargar()

        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.lista is ListaUi.Contenido)
        val contenido = state.lista as ListaUi.Contenido
        assertEquals(1, contenido.recetas.size)
        assertEquals("Ensalada del Chef", contenido.recetas[0].titulo)
    }

    @Test
    fun eliminacion_enRecetaNoPropia_esRechazada() {
        recetaRepository.recetas.add(recetaGlobal)
        viewModel.configurarModo(ModoHome.EXPLORAR)
        viewModel.cargar()

        viewModel.onEliminarClick(recetaGlobal)
        assertNull(viewModel.uiState.value.recetaAEliminar)

        viewModel.onConfirmarEliminar()
        assertEquals(0, recetaRepository.llamadasEliminar)
    }

    @Test
    fun eliminacion_enRecetaPropiaVerificada_esExitosa() = runTest {
        recetaRepository.recetas.add(recetaPropia)
        recetaRepository.registrarRecetaPropia(recetaPropia.id)

        viewModel.configurarModo(ModoHome.MIS_RECETAS)
        viewModel.cargar()
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onEliminarClick(recetaPropia)
        assertEquals(recetaPropia, viewModel.uiState.value.recetaAEliminar)

        viewModel.onConfirmarEliminar()
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(1, recetaRepository.llamadasEliminar)
        assertNull(viewModel.uiState.value.recetaAEliminar)
        assertEquals("Receta eliminada", viewModel.uiState.value.mensaje)
    }

    @Test
    fun onCerrarSesion_limpiaSesionYCambiaAModoExplorar() {
        viewModel.onCerrarSesion()

        val state = viewModel.uiState.value
        assertTrue(state.sesionCerrada)
        assertFalse(state.estaAutenticado)
        assertEquals(ModoHome.EXPLORAR, state.modo)
        assertFalse(authRepository.haySesion())
    }
}
