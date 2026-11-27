package com.perezgomez.recetario.features.recetas.domain.usecases

import com.perezgomez.recetario.features.auth.domain.entities.Usuario
import com.perezgomez.recetario.features.recetas.domain.entities.Receta
import com.perezgomez.recetario.util.FakeAuthRepository
import com.perezgomez.recetario.util.FakeRecetaRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class EliminarRecetaPropiaUseCaseTest {

    private lateinit var recetaRepository: FakeRecetaRepository
    private lateinit var authRepository: FakeAuthRepository

    private val recetaAjena = Receta(
        id = "receta-ajena-1",
        titulo = "Pasta Ajena",
        categoria = "comida",
        tiempoMinutos = 30,
        porciones = 2,
        ingredientes = emptyList(),
        pasos = emptyList(),
    )

    private val recetaPropia = Receta(
        id = "receta-propia-1",
        titulo = "Café de Olla",
        categoria = "bebida",
        tiempoMinutos = 10,
        porciones = 3,
        ingredientes = emptyList(),
        pasos = emptyList(),
        esPropia = true,
    )

    @Before
    fun setUp() {
        recetaRepository = FakeRecetaRepository()
        authRepository = FakeAuthRepository(Usuario("user-1", "Carlos", "carlos@test.com"))
        recetaRepository.recetas.addAll(listOf(recetaAjena, recetaPropia))
    }

    @Test
    fun usuarioSinSesion_bloqueaEliminacionYNoLlamaRepositorio() = runTest {
        val useCase = EliminarRecetaPropiaUseCase(recetaRepository, FakeAuthRepository(null))

        val resultado = useCase(recetaPropia.id, esPropia = recetaPropia.esPropia)

        assertTrue(resultado.isFailure)
        assertEquals(0, recetaRepository.llamadasEliminar)
        assertEquals(2, recetaRepository.recetas.size)
    }

    @Test
    fun recetaAjena_bloqueaEliminacionYNoLlamaRepositorio() = runTest {
        val useCase = EliminarRecetaPropiaUseCase(recetaRepository, authRepository)

        val resultado = useCase(recetaAjena.id, esPropia = recetaAjena.esPropia)

        assertTrue(resultado.isFailure)
        assertEquals(0, recetaRepository.llamadasEliminar)
        assertTrue(recetaRepository.recetas.any { it.id == recetaAjena.id })
    }

    @Test
    fun recetaPropiaConSesionActiva_eliminaEnRepositorioConExito() = runTest {
        val useCase = EliminarRecetaPropiaUseCase(recetaRepository, authRepository)

        val resultado = useCase(recetaPropia.id, esPropia = recetaPropia.esPropia)

        assertTrue(resultado.isSuccess)
        assertEquals(1, recetaRepository.llamadasEliminar)
        assertTrue(recetaRepository.recetas.none { it.id == recetaPropia.id })
    }

    @Test
    fun errorEnRepositorio_propagaFallo() = runTest {
        recetaRepository.deberiaFallar = true
        recetaRepository.mensajeError = "No se pudo eliminar en el servidor"
        val useCase = EliminarRecetaPropiaUseCase(recetaRepository, authRepository)

        val resultado = useCase(recetaPropia.id, esPropia = recetaPropia.esPropia)

        assertTrue(resultado.isFailure)
        assertEquals(1, recetaRepository.llamadasEliminar)
        assertEquals("No se pudo eliminar en el servidor", resultado.exceptionOrNull()?.message)
    }
}
