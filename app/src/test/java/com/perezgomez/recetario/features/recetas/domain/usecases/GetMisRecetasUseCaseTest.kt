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

class GetMisRecetasUseCaseTest {

    private lateinit var recetaRepository: FakeRecetaRepository
    private lateinit var authRepository: FakeAuthRepository

    private val recetaAjena = Receta(
        id = "receta-ajena-1",
        titulo = "Sopa Comunitaria",
        categoria = "comida",
        tiempoMinutos = 25,
        porciones = 4,
        ingredientes = emptyList(),
        pasos = emptyList(),
    )

    private val recetaPropia = Receta(
        id = "receta-propia-1",
        titulo = "Chilaquiles de Casa",
        categoria = "desayuno",
        tiempoMinutos = 15,
        porciones = 2,
        ingredientes = emptyList(),
        pasos = emptyList(),
    )

    @Before
    fun setUp() {
        recetaRepository = FakeRecetaRepository()
        authRepository = FakeAuthRepository(Usuario("user-1", "Carlos", "carlos@test.com"))
    }

    @Test
    fun usuarioSinSesion_devuelveListaVaciaYNoConsultaRepositorio() = runTest {
        recetaRepository.recetas.addAll(listOf(recetaAjena, recetaPropia))
        recetaRepository.registrarRecetaPropia(recetaPropia.id)
        val useCase = GetMisRecetasUseCase(recetaRepository, FakeAuthRepository(null))

        val resultado = useCase()

        assertTrue(resultado.isSuccess)
        assertTrue(resultado.getOrNull().orEmpty().isEmpty())
        assertEquals(0, recetaRepository.llamadasListar)
    }

    @Test
    fun usuarioAutenticado_conRecetasPropiasYAjenas_filtraSoloLasPropias() = runTest {
        recetaRepository.recetas.addAll(listOf(recetaAjena, recetaPropia))
        recetaRepository.registrarRecetaPropia(recetaPropia.id)
        val useCase = GetMisRecetasUseCase(recetaRepository, authRepository)

        val resultado = useCase()

        assertTrue(resultado.isSuccess)
        val lista = resultado.getOrNull().orEmpty()
        assertEquals(1, lista.size)
        assertEquals("receta-propia-1", lista.first().id)
        assertEquals(1, recetaRepository.llamadasListar)
    }

    @Test
    fun usuarioAutenticado_soloRecetasAjenas_devuelveListaVacia() = runTest {
        recetaRepository.recetas.add(recetaAjena)
        val useCase = GetMisRecetasUseCase(recetaRepository, authRepository)

        val resultado = useCase()

        assertTrue(resultado.isSuccess)
        assertTrue(resultado.getOrNull().orEmpty().isEmpty())
    }

    @Test
    fun errorEnRepositorio_propagaFalloConMensaje() = runTest {
        recetaRepository.deberiaFallar = true
        recetaRepository.mensajeError = "Error de conexión simulado"
        val useCase = GetMisRecetasUseCase(recetaRepository, authRepository)

        val resultado = useCase()

        assertTrue(resultado.isFailure)
        assertEquals("Error de conexión simulado", resultado.exceptionOrNull()?.message)
    }
}
