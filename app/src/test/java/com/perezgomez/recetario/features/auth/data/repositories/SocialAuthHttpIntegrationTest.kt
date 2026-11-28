package com.perezgomez.recetario.features.auth.data.repositories

import com.perezgomez.recetario.core.domain.error.UnauthorizedException
import com.perezgomez.recetario.core.network.ApiCaller
import com.perezgomez.recetario.core.network.AuthInterceptor
import com.perezgomez.recetario.features.auth.data.datasources.remote.api.AuthApiService
import com.perezgomez.recetario.features.recetas.data.datasources.remote.api.RecetaApiService
import com.perezgomez.recetario.features.recetas.data.repositories.RecetaRepositoryImpl
import com.perezgomez.recetario.util.FakeSessionStorage
import com.perezgomez.recetario.util.FakeVisitorStorage
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicInteger
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

/**
 * Pruebas HTTP reales usando OkHttp Interceptor + AuthInterceptor + Retrofit,
 * verificando identidad anónima firmada, concurrencia sin duplicados, migración incompleta,
 * cambio de cuenta, rotación y errores HTTP 401.
 */
class SocialAuthHttpIntegrationTest {

    private lateinit var sessionStorage: FakeSessionStorage
    private lateinit var visitorStorage: FakeVisitorStorage
    private lateinit var authApi: AuthApiService
    private lateinit var recetaApi: RecetaApiService
    private lateinit var visitorManager: VisitorIdentityManager
    private lateinit var authRepository: AuthRepositoryImpl
    private lateinit var recetaRepository: RecetaRepositoryImpl

    private val peticiones = CopyOnWriteArrayList<RecordedRequest>()
    private val contadorVisitante = AtomicInteger(0)
    private var respuestaLoginMigracionJson: String =
        """{"intentada":true,"completada":true,"migrados":1,"deduplicados":0,"bloqueadaPorOtraCuenta":false,"parcial":false}"""
    private var devolver401EnLike = false

    data class RecordedRequest(
        val method: String,
        val path: String,
        val authHeader: String?,
        val visitorTokenHeader: String?,
    )

    @Before
    fun setUp() {
        sessionStorage = FakeSessionStorage(tokenActual = null, usuarioActual = null)
        visitorStorage = FakeVisitorStorage()
        val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }
        val apiCaller = ApiCaller(json)

        val mockHttpInterceptor = Interceptor { chain ->
            val req = chain.request()
            val path = req.url.encodedPath
            val method = req.method
            val auth = req.header("Authorization")
            val vis = req.header("X-Visitor-Token")
            peticiones.add(RecordedRequest(method, path, auth, vis))

            val (code, body) = when {
                method == "POST" && path == "/visitante/sesion" -> {
                    val n = contadorVisitante.incrementAndGet()
                    201 to """{"visitorId":"vis-$n","visitorToken":"vis-$n.firma-$n"}"""
                }
                method == "POST" && path == "/recetas/r-1/like" -> {
                    if (devolver401EnLike) {
                        401 to """{"error":"No autorizado"}"""
                    } else {
                        200 to """{"recetaId":"r-1","likesCount":4,"leDiLike":true}"""
                    }
                }
                method == "POST" && path == "/login" -> {
                    200 to """{"token":"jwt-ana","usuario":{"id":"u-ana","nombre":"Ana","correo":"ana@test.com"},"migracionLikes":$respuestaLoginMigracionJson}"""
                }
                method == "POST" && path == "/register" -> {
                    201 to """{"token":"jwt-nuevo","usuario":{"id":"u-nuevo","nombre":"Nuevo","correo":"nuevo@test.com"},"migracionLikes":$respuestaLoginMigracionJson}"""
                }
                else -> 404 to """{"error":"No encontrado"}"""
            }

            Response.Builder()
                .request(req)
                .protocol(Protocol.HTTP_1_1)
                .code(code)
                .message(if (code in 200..299) "OK" else "Error")
                .body(body.toResponseBody("application/json; charset=utf-8".toMediaType()))
                .build()
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(sessionStorage, visitorStorage))
            .addInterceptor(mockHttpInterceptor)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl("http://localhost/")
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json; charset=UTF-8".toMediaType()))
            .build()

        authApi = retrofit.create(AuthApiService::class.java)
        recetaApi = retrofit.create(RecetaApiService::class.java)
        visitorManager = VisitorIdentityManager(authApi, visitorStorage, apiCaller)
        authRepository = AuthRepositoryImpl(authApi, sessionStorage, visitorStorage, apiCaller)
        recetaRepository = RecetaRepositoryImpl(recetaApi, sessionStorage, visitorManager, apiCaller)
    }

    @After
    fun tearDown() {
        peticiones.clear()
    }

    @Test
    fun visitante_solicitudesConcurrentes_generanUnaSolaIdentidadYSeReutiliza() = runTest {
        val tokens = (1..5).map { async { visitorManager.asegurarTokenVisitante().getOrThrow() } }.awaitAll()

        assertEquals(1, contadorVisitante.get())
        assertTrue(tokens.all { it == "vis-1.firma-1" })

        val likeRes = recetaRepository.darLike("r-1").getOrThrow()
        assertEquals(4, likeRes.likesCount)
        assertTrue(likeRes.leDiLike)

        val likeReq = peticiones.last { it.path == "/recetas/r-1/like" }
        assertNull(likeReq.authHeader)
        assertEquals("vis-1.firma-1", likeReq.visitorTokenHeader)
    }

    @Test
    fun usuarioRegistrado_priorizaBearer_y401MapeaAUnauthorizedException() = runTest {
        visitorStorage.guardarIdentidadVisitante("vis-1", "vis-1.firma-1")
        sessionStorage.guardarSesion("jwt-activo", com.perezgomez.recetario.features.auth.domain.entities.Usuario("u1", "Ana", "ana@test.com"))

        recetaRepository.darLike("r-1").getOrThrow()
        val req = peticiones.last { it.path == "/recetas/r-1/like" }
        assertEquals("Bearer jwt-activo", req.authHeader)
        assertNull(req.visitorTokenHeader)

        devolver401EnLike = true
        val fallo = recetaRepository.darLike("r-1")
        assertTrue(fallo.exceptionOrNull() is UnauthorizedException)
    }

    @Test
    fun migracionIncompleta_conservaTokenParaMismaCuenta_yBloqueaParaCuentaDiferente() = runTest {
        visitorStorage.guardarIdentidadVisitante("vis-9", "vis-9.firma-9")
        respuestaLoginMigracionJson =
            """{"intentada":true,"completada":false,"migrados":1,"deduplicados":0,"bloqueadaPorOtraCuenta":false,"parcial":true}"""

        // Ana inicia sesión y la migración queda parcial -> token NO se elimina, queda ligado a Ana
        authRepository.iniciarSesion("ana@test.com", "Password123").getOrThrow()
        assertTrue(visitorStorage.hayMigracionPendiente())
        assertNull(visitorStorage.visitorTokenSync())

        // Ana cierra sesión e intenta entrar Beto en el mismo dispositivo -> NO recibe el token pendiente de Ana
        authRepository.cerrarSesion()
        assertTrue(visitorStorage.hayMigracionPendiente())
        authRepository.iniciarSesion("beto@test.com", "Password123").getOrThrow()
        val reqBeto = peticiones.last { it.path == "/login" }
        assertNull(reqBeto.visitorTokenHeader)

        // Al volver Ana a iniciar sesión con migración completada -> sí reenvía vis-9 y rota al terminar
        respuestaLoginMigracionJson =
            """{"intentada":true,"completada":true,"migrados":1,"deduplicados":0,"bloqueadaPorOtraCuenta":false,"parcial":false}"""
        authRepository.cerrarSesion()
        authRepository.iniciarSesion("ana@test.com", "Password123").getOrThrow()
        val reqAna = peticiones.last { it.path == "/login" }
        assertEquals("vis-9.firma-9", reqAna.visitorTokenHeader)
        assertFalse(visitorStorage.hayMigracionPendiente())
    }
}
