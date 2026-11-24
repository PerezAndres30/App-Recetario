package com.perezgomez.recetario.data.repository

import com.perezgomez.recetario.data.datasource.RecetaApiService
import com.perezgomez.recetario.data.dto.RecetaDto
import com.perezgomez.recetario.data.dto.RecetaRequestDto
import com.perezgomez.recetario.data.dto.RecetasResponseDto
import com.perezgomez.recetario.data.remote.ApiCaller
import com.perezgomez.recetario.domain.model.Ingrediente
import com.perezgomez.recetario.domain.model.Receta
import com.perezgomez.recetario.domain.model.Usuario
import com.perezgomez.recetario.util.FakeSessionStorage
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class RecetaRepositoryImplTest {

    private lateinit var fakeApi: FakeRecetaApiService
    private lateinit var sessionStorage: FakeSessionStorage
    private lateinit var apiCaller: ApiCaller
    private lateinit var repository: RecetaRepositoryImpl

    private val usuarioA = Usuario("user-A", "Alice", "alice@test.com")
    private val usuarioB = Usuario("user-B", "Bob", "bob@test.com")

    @Before
    fun setUp() {
        fakeApi = FakeRecetaApiService()
        sessionStorage = FakeSessionStorage(usuarioActual = usuarioA)
        apiCaller = ApiCaller(Json { ignoreUnknownKeys = true })
        repository = RecetaRepositoryImpl(fakeApi, apiCaller, sessionStorage)
    }

    @Test
    fun esPropietario_sinSesionActiva_retornaFalse() {
        sessionStorage.limpiar()

        assertFalse(repository.esPropietario("receta-1"))
    }

    @Test
    fun registrarRecetaPropia_usuarioA_noOtorgaPropiedadAUsuarioB() {
        // 1. Usuario A registra autoría de una receta
        sessionStorage.guardarSesion("token-A", usuarioA)
        repository.registrarRecetaPropia("receta-privada-A")
        assertTrue(repository.esPropietario("receta-privada-A"))

        // 2. Usuario A cierra sesión -> sin sesión no es propietario
        sessionStorage.limpiar()
        assertFalse(repository.esPropietario("receta-privada-A"))

        // 3. Usuario B inicia sesión en la misma app -> no hereda propiedad de Usuario A
        sessionStorage.guardarSesion("token-B", usuarioB)
        assertFalse(repository.esPropietario("receta-privada-A"))

        // 4. Usuario A vuelve a iniciar sesión -> recupera su propiedad
        sessionStorage.guardarSesion("token-A", usuarioA)
        assertTrue(repository.esPropietario("receta-privada-A"))
    }

    @Test
    fun crearReceta_asociaPropiedadExclusivamenteAlUsuarioActual() = runTest {
        sessionStorage.guardarSesion("token-A", usuarioA)

        val receta = Receta(
            id = "",
            titulo = "Tacos de Alice",
            descripcion = "Receta secreta",
            categoria = "comida",
            tiempoMinutos = 20,
            porciones = 2,
            ingredientes = listOf(Ingrediente("Tortillas"), Ingrediente("Carne")),
            pasos = listOf("Calentar", "Servir"),
        )

        val resultado = repository.crear(receta)
        assertTrue(resultado.isSuccess)
        val idCreado = resultado.getOrThrow().id

        // Usuario A es propietario
        assertTrue(repository.esPropietario(idCreado))

        // Usuario B NO es propietario de la receta creada por Usuario A
        sessionStorage.guardarSesion("token-B", usuarioB)
        assertFalse(repository.esPropietario(idCreado))
    }

    @Test
    fun eliminarReceta_remuevePropiedadDelUsuarioActual() = runTest {
        sessionStorage.guardarSesion("token-A", usuarioA)
        repository.registrarRecetaPropia("receta-a-borrar")
        fakeApi.recetasDto.add(
            RecetaDto(
                recetaId = "receta-a-borrar",
                titulo = "A borrar",
                descripcion = "",
                categoria = "comida",
                tiempoMinutos = 5,
                porciones = 1,
                ingredientes = emptyList(),
                pasos = emptyList(),
            )
        )

        assertTrue(repository.esPropietario("receta-a-borrar"))

        val resultado = repository.eliminar("receta-a-borrar")
        assertTrue(resultado.isSuccess)
        assertFalse(repository.esPropietario("receta-a-borrar"))
    }

    private class FakeRecetaApiService : RecetaApiService {
        val recetasDto = mutableListOf<RecetaDto>()

        override suspend fun listar(): RecetasResponseDto = RecetasResponseDto(recetas = recetasDto)
        override suspend fun obtener(id: String): RecetaDto = recetasDto.first { it.recetaId == id }
        override suspend fun crear(body: RecetaRequestDto): RecetaDto {
            val dto = RecetaDto(
                recetaId = "receta-${recetasDto.size + 1}",
                titulo = body.titulo,
                descripcion = body.descripcion,
                categoria = body.categoria,
                tiempoMinutos = body.tiempoMinutos,
                porciones = body.porciones,
                ingredientes = body.ingredientes,
                pasos = body.pasos,
            )
            recetasDto.add(dto)
            return dto
        }
        override suspend fun actualizar(id: String, body: RecetaRequestDto): RecetaDto {
            val index = recetasDto.indexOfFirst { it.recetaId == id }
            val updated = RecetaDto(
                recetaId = id,
                titulo = body.titulo,
                descripcion = body.descripcion,
                categoria = body.categoria,
                tiempoMinutos = body.tiempoMinutos,
                porciones = body.porciones,
                ingredientes = body.ingredientes,
                pasos = body.pasos,
            )
            if (index != -1) recetasDto[index] = updated
            return updated
        }
        override suspend fun eliminar(id: String) {
            recetasDto.removeAll { it.recetaId == id }
        }
    }
}
