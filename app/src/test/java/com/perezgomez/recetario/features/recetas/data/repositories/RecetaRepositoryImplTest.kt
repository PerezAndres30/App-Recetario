package com.perezgomez.recetario.features.recetas.data.repositories

import com.perezgomez.recetario.core.network.ApiCaller
import com.perezgomez.recetario.features.auth.data.datasources.remote.api.AuthApiService
import com.perezgomez.recetario.features.auth.data.datasources.remote.models.LoginRequestDto
import com.perezgomez.recetario.features.auth.data.datasources.remote.models.RegistroRequestDto
import com.perezgomez.recetario.features.auth.data.datasources.remote.models.SesionDto
import com.perezgomez.recetario.features.auth.data.datasources.remote.models.UsuarioDto
import com.perezgomez.recetario.features.auth.data.datasources.remote.models.VisitanteSesionDto
import com.perezgomez.recetario.features.auth.data.repositories.VisitorIdentityManager
import com.perezgomez.recetario.features.recetas.data.datasources.remote.api.RecetaApiService
import com.perezgomez.recetario.features.recetas.data.datasources.remote.mapper.toRequestDto
import com.perezgomez.recetario.features.recetas.data.datasources.remote.models.FavoritoRespuestaDto
import com.perezgomez.recetario.features.recetas.data.datasources.remote.models.FirmaImagenDto
import com.perezgomez.recetario.features.recetas.data.datasources.remote.models.LikeRespuestaDto
import com.perezgomez.recetario.features.recetas.data.datasources.remote.models.RecetaDto
import com.perezgomez.recetario.features.recetas.data.datasources.remote.models.RecetaRequestDto
import com.perezgomez.recetario.features.recetas.data.datasources.remote.models.RecetasResponseDto
import com.perezgomez.recetario.features.recetas.domain.entities.CambioImagen
import com.perezgomez.recetario.features.recetas.domain.entities.Receta
import com.perezgomez.recetario.util.FakeSessionStorage
import com.perezgomez.recetario.util.FakeVisitorStorage
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class RecetaRepositoryImplTest {

    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }
    private lateinit var fakeApi: FakeRecetaApiService
    private lateinit var sessionStorage: FakeSessionStorage
    private lateinit var visitorStorage: FakeVisitorStorage
    private lateinit var repository: RecetaRepositoryImpl

    private val receta = Receta(
        id = "receta-1",
        titulo = "Sopa",
        categoria = "comida",
        tiempoMinutos = 10,
        porciones = 2,
        ingredientes = emptyList(),
        pasos = listOf("Hervir"),
    )

    @Before
    fun setUp() {
        fakeApi = FakeRecetaApiService()
        sessionStorage = FakeSessionStorage()
        visitorStorage = FakeVisitorStorage()
        val apiCaller = ApiCaller(json)
        val visitorManager = VisitorIdentityManager(FakeAuthApiService(), visitorStorage, apiCaller)
        repository = RecetaRepositoryImpl(fakeApi, sessionStorage, visitorManager, apiCaller)
    }

    private fun dto(id: String, esPropia: Boolean, imagenUrl: String? = null) = RecetaDto(
        recetaId = id,
        titulo = "R $id",
        esPropia = esPropia,
        imagenUrl = imagenUrl,
    )

    @Test
    fun listar_publico_noPideSoloMias() = runTest {
        fakeApi.recetas += dto("a", esPropia = false)
        repository.listar(soloMias = false)

        assertEquals(null, fakeApi.ultimoMias)
    }

    @Test
    fun listar_soloMias_pideMiasTrueYConservaEsPropiaDeLaApi() = runTest {
        fakeApi.recetas += dto("a", esPropia = true)
        fakeApi.recetas += dto("b", esPropia = false)

        val resultado = repository.listar(soloMias = true).getOrThrow()

        assertEquals(true, fakeApi.ultimoMias)
        assertEquals(listOf("a"), resultado.map { it.id })
        assertTrue(resultado[0].esPropia)
    }

    @Test
    fun obtener_mapeaImagenYPropiedad() = runTest {
        fakeApi.recetas += dto("a", esPropia = true, imagenUrl = "https://res.cloudinary.com/x/image/upload/recetario/recetas/a.jpg")

        val r = repository.obtener("a").getOrThrow()

        assertTrue(r.esPropia)
        assertEquals("https://res.cloudinary.com/x/image/upload/recetario/recetas/a.jpg", r.imagenUrl)
    }

    @Test
    fun recetaSinFoto_imagenUrlEsNull() = runTest {
        fakeApi.recetas += dto("a", esPropia = false)
        assertEquals(null, repository.obtener("a").getOrThrow().imagenUrl)
    }

    @Test
    fun eliminar_llamaALaApi() = runTest {
        fakeApi.recetas += dto("a", esPropia = true)
        assertTrue(repository.eliminar("a").isSuccess)
        assertFalse(fakeApi.recetas.any { it.recetaId == "a" })
    }

    // ---- la API distingue "omitido", null y texto en imagenUrl/imagenPublicId ----

    private fun cuerpo(imagen: CambioImagen): JsonObject =
        json.encodeToJsonElement(RecetaRequestDto.serializer(), receta.toRequestDto(imagen)).jsonObject

    @Test
    fun cuerpoMantener_noIncluyeLosCamposDeImagen() {
        val c = cuerpo(CambioImagen.Mantener)
        assertFalse(c.containsKey("imagenUrl"))
        assertFalse(c.containsKey("imagenPublicId"))
    }

    @Test
    fun cuerpoQuitar_enviaAmbosCamposEnNull() {
        val c = cuerpo(CambioImagen.Quitar)
        assertEquals(JsonNull, c["imagenUrl"])
        assertEquals(JsonNull, c["imagenPublicId"])
    }

    @Test
    fun cuerpoEstablecer_enviaUrlYPublicId() {
        val c = cuerpo(CambioImagen.Establecer("https://x/y.jpg", "recetario/recetas/receta-1"))
        assertEquals(JsonPrimitive("https://x/y.jpg"), c["imagenUrl"])
        assertEquals(JsonPrimitive("recetario/recetas/receta-1"), c["imagenPublicId"])
    }

    private class FakeRecetaApiService : RecetaApiService {
        val recetas = mutableListOf<RecetaDto>()
        var ultimoMias: Boolean? = null

        override suspend fun listar(mias: Boolean?): RecetasResponseDto {
            ultimoMias = mias
            return RecetasResponseDto(recetas = if (mias == true) recetas.filter { it.esPropia } else recetas)
        }

        override suspend fun obtener(id: String): RecetaDto = recetas.first { it.recetaId == id }

        override suspend fun crear(body: RecetaRequestDto): RecetaDto {
            val d = RecetaDto(recetaId = "receta-${recetas.size + 1}", titulo = body.titulo, esPropia = true)
            recetas.add(d)
            return d
        }

        override suspend fun actualizar(id: String, body: RecetaRequestDto): RecetaDto =
            recetas.first { it.recetaId == id }

        override suspend fun firmarImagen(id: String): FirmaImagenDto = FirmaImagenDto(
            uploadUrl = "https://api.cloudinary.com/v1_1/demo/image/upload",
            cloudName = "demo",
            apiKey = "k",
            timestamp = 1L,
            signature = "s",
            publicId = "recetario/recetas/$id",
            allowedFormats = "jpg,png,webp",
            overwrite = true,
            invalidate = true,
        )

        override suspend fun eliminar(id: String) {
            recetas.removeAll { it.recetaId == id }
        }

        override suspend fun darLike(id: String): LikeRespuestaDto =
            LikeRespuestaDto(recetaId = id, likesCount = 1, leDiLike = true)

        override suspend fun quitarLike(id: String): LikeRespuestaDto =
            LikeRespuestaDto(recetaId = id, likesCount = 0, leDiLike = false)

        override suspend fun listarFavoritos(): RecetasResponseDto =
            RecetasResponseDto(recetas = recetas.filter { it.esFavorito })

        override suspend fun guardarFavorito(id: String): FavoritoRespuestaDto =
            FavoritoRespuestaDto(recetaId = id, esFavorito = true)

        override suspend fun quitarFavorito(id: String): FavoritoRespuestaDto =
            FavoritoRespuestaDto(recetaId = id, esFavorito = false)
    }

    private class FakeAuthApiService : AuthApiService {
        override suspend fun crearSesionVisitante() =
            VisitanteSesionDto(visitorId = "v-1", visitorToken = "v-1.sig")

        override suspend fun registrar(body: RegistroRequestDto, visitorToken: String?) =
            SesionDto("t", UsuarioDto("u", body.nombre, body.correo))

        override suspend fun iniciarSesion(body: LoginRequestDto, visitorToken: String?) =
            SesionDto("t", UsuarioDto("u", "Chef", body.correo))
    }

    @Test
    fun toRequestDto_sinFavorita_omiteCampoParaNoSobrescribirHistorico() {
        val obj = json.encodeToJsonElement(
            RecetaRequestDto.serializer(),
            receta.copy(favorita = true).toRequestDto(incluirFavorita = false),
        ).jsonObject
        assertFalse(obj.containsKey("favorita"))
    }
}
