package com.perezgomez.recetario.features.recetas.presentation.viewmodels

import com.perezgomez.recetario.features.auth.domain.entities.Usuario
import com.perezgomez.recetario.features.recetas.domain.entities.Receta
import com.perezgomez.recetario.features.recetas.domain.usecases.AlternarFavoritoUseCase
import com.perezgomez.recetario.features.recetas.domain.usecases.GetFavoritosUseCase
import com.perezgomez.recetario.features.recetas.presentation.screens.FavoritosListaUi
import com.perezgomez.recetario.util.FakeAuthRepository
import com.perezgomez.recetario.util.FakeRecetaRepository
import com.perezgomez.recetario.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FavoritosViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var recetaRepository: FakeRecetaRepository
    private lateinit var authRepository: FakeAuthRepository

    private val propiaFavorita = Receta(
        id = "propia-1",
        titulo = "Panqué Casero",
        categoria = "postre",
        tiempoMinutos = 40,
        porciones = 6,
        ingredientes = emptyList(),
        pasos = listOf("Hornear"),
        esPropia = true,
        esFavorito = true,
        likesCount = 5,
    )

    private val ajenaFavorita = Receta(
        id = "ajena-2",
        titulo = "Sopa de Tortilla",
        categoria = "comida",
        tiempoMinutos = 30,
        porciones = 4,
        ingredientes = emptyList(),
        pasos = listOf("Servir"),
        esPropia = false,
        esFavorito = true,
        likesCount = 12,
    )

    @Before
    fun setUp() {
        recetaRepository = FakeRecetaRepository()
        authRepository = FakeAuthRepository(Usuario("user-1", "Ana Sofía", "ana@test.com"))
        recetaRepository.recetas.addAll(listOf(propiaFavorita, ajenaFavorita))
    }

    private fun crearViewModel(auth: FakeAuthRepository = authRepository) = FavoritosViewModel(
        recetaRepository = recetaRepository,
        authRepository = auth,
        getFavoritosUseCase = GetFavoritosUseCase(recetaRepository, auth),
        alternarFavoritoUseCase = AlternarFavoritoUseCase(recetaRepository, auth),
    )

    @Test
    fun cargar_muestraFavoritosPropiosYAjenos_yVisitanteCierraEstadoPrivado() = runTest {
        val vm = crearViewModel()
        vm.cargar()
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        val contenido = vm.uiState.value.lista as FavoritosListaUi.Contenido
        assertEquals(listOf("propia-1", "ajena-2"), contenido.recetas.map { it.id })

        val llamadasAntes = recetaRepository.llamadasListarFavoritos
        val vmVisitante = crearViewModel(FakeAuthRepository(null))
        vmVisitante.cargar()
        assertTrue(vmVisitante.uiState.value.sesionCerrada)
        assertTrue(vmVisitante.uiState.value.lista is FavoritosListaUi.Vacia)
        assertEquals(llamadasAntes, recetaRepository.llamadasListarFavoritos)
    }

    @Test
    fun quitarFavorito_yLike_sincronizan_yErrorDeRedRevierte() = runTest {
        val vm = crearViewModel()
        vm.cargar()
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        vm.onToggleLike("ajena-2")
        vm.onQuitarFavorito("propia-1")
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        val actual = vm.uiState.value.lista as FavoritosListaUi.Contenido
        assertEquals(listOf("ajena-2"), actual.recetas.map { it.id })
        assertEquals(13, actual.recetas[0].likesCount)
        assertTrue(actual.recetas[0].leDiLike)

        // Error al quitar último favorito debe restaurar lista
        recetaRepository.deberiaFallar = true
        vm.onQuitarFavorito("ajena-2")
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        val restaurada = vm.uiState.value.lista as FavoritosListaUi.Contenido
        assertEquals(1, restaurada.recetas.size)
        assertEquals("Error simulado en repositorio", vm.uiState.value.mensaje)
    }

    @Test
    fun error401_yCerrarSesion_limpianColeccionPrivada() = runTest {
        val vm = crearViewModel()
        vm.cargar()
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        recetaRepository.simularUnauthorized = true
        vm.onToggleLike("propia-1")
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(vm.uiState.value.sesionExpirada)
        assertTrue(vm.uiState.value.lista is FavoritosListaUi.Vacia)
        assertFalse(authRepository.haySesion())
    }
}
