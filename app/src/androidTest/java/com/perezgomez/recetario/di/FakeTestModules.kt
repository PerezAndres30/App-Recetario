package com.perezgomez.recetario.di

import com.perezgomez.recetario.core.network.TokenProvider
import com.perezgomez.recetario.core.network.VisitorTokenProvider
import com.perezgomez.recetario.features.auth.di.AuthRepositoryModule
import com.perezgomez.recetario.features.auth.domain.entities.Usuario
import com.perezgomez.recetario.features.auth.domain.repositories.AuthRepository
import com.perezgomez.recetario.features.auth.domain.repositories.SessionStorage
import com.perezgomez.recetario.features.auth.domain.repositories.VisitorStorage
import com.perezgomez.recetario.features.recetas.di.RecetasRepositoryModule
import com.perezgomez.recetario.features.recetas.domain.entities.CambioImagen
import com.perezgomez.recetario.features.recetas.domain.entities.EventoSocialReceta
import com.perezgomez.recetario.features.recetas.domain.entities.ImagenSubida
import com.perezgomez.recetario.features.recetas.domain.entities.Ingrediente
import com.perezgomez.recetario.features.recetas.domain.entities.Receta
import com.perezgomez.recetario.features.recetas.domain.entities.ResultadoLike
import com.perezgomez.recetario.features.recetas.domain.imagen.ImagenLista
import com.perezgomez.recetario.features.recetas.domain.imagen.ProcesadorImagen
import com.perezgomez.recetario.features.recetas.domain.repositories.ImagenRepository
import com.perezgomez.recetario.features.recetas.domain.repositories.RecetaRepository
import com.perezgomez.recetario.features.recetas.domain.tts.RecetaTtsService
import com.perezgomez.recetario.features.recetas.domain.tts.RecetaTtsServiceImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Repositorios simulados exclusivamente para pruebas instrumentadas en src/androidTest.
 * Evita cualquier petición externa a AWS mientras conserva real el motor AndroidTtsEngine
 * y el servicio RecetaTtsServiceImpl.
 */
@Singleton
class InMemorySessionStorage @Inject constructor() : SessionStorage {
    var tokenActual: String? = null
    var usuarioActual: Usuario? = null

    override fun guardarSesion(token: String, usuario: Usuario) {
        tokenActual = token
        usuarioActual = usuario
    }

    override fun token(): String? = tokenActual

    override fun usuario(): Usuario? = usuarioActual

    override fun limpiar() {
        tokenActual = null
        usuarioActual = null
    }
}

@Singleton
class InMemoryVisitorStorage @Inject constructor() : VisitorStorage {
    var activeToken: String? = "visitor-test.firma"
    override fun guardarIdentidadVisitante(visitorId: String, visitorToken: String) {
        activeToken = visitorToken
    }
    override fun visitorTokenSync(): String? = activeToken
    override fun visitorIdSync(): String? = "visitor-test"
    override fun tokenParaLogin(correo: String): String? = activeToken
    override fun tokenParaRegistro(): String? = activeToken
    override fun registrarMigracionPendiente(userId: String, correo: String, token: String) = Unit
    override fun completarORetirarTokenUsado(tokenUsado: String, userId: String?, limpiarPendienteDeCuenta: Boolean) = Unit
    override fun rotarIdentidadVisitante() { activeToken = "visitor-rotated.firma" }
    override fun hayMigracionPendiente(): Boolean = false
}

@Singleton
class FakeAndroidAuthRepository @Inject constructor(
    private val session: InMemorySessionStorage,
) : AuthRepository {

    fun establecerSesion(usuario: Usuario?) {
        if (usuario == null) {
            session.limpiar()
        } else {
            session.guardarSesion("token-test-instrumentado", usuario)
        }
    }

    override suspend fun registrar(nombre: String, correo: String, password: String): Result<Usuario> {
        val nuevo = Usuario(id = "user-reg-1", nombre = nombre, correo = correo)
        session.guardarSesion("token-test-reg", nuevo)
        return Result.success(nuevo)
    }

    override suspend fun iniciarSesion(correo: String, password: String): Result<Usuario> {
        val usuario = Usuario(id = "user-login-1", nombre = "Chef Carlos", correo = correo)
        session.guardarSesion("token-test-login", usuario)
        return Result.success(usuario)
    }

    override fun haySesion(): Boolean = !session.token().isNullOrBlank()

    override fun usuarioActual(): Usuario? = session.usuario()

    override fun cerrarSesion() = session.limpiar()
}

@Singleton
class FakeAndroidRecetaRepository @Inject constructor() : RecetaRepository {

    val recetas = mutableListOf<Receta>()
    private val _eventosSociales = MutableSharedFlow<EventoSocialReceta>(extraBufferCapacity = 32)
    override val eventosSociales: Flow<EventoSocialReceta> = _eventosSociales.asSharedFlow()
    private var contadorId = 100

    fun reiniciarDatosDePrueba() {
        recetas.clear()
        contadorId = 100

        val comunitaria = Receta(
            id = "receta-comunitaria-1",
            titulo = "Mole Poblano Comunitario",
            descripcion = "Receta compartida por la comunidad sin autoría del usuario actual.",
            categoria = "comida",
            tiempoMinutos = 60,
            porciones = 6,
            ingredientes = listOf(
                Ingrediente("Pollo", "1", "kg"),
                Ingrediente("Pasta de mole", "250", "g"),
            ),
            pasos = listOf(
                "Cocer el pollo en agua con cebolla y sal.",
                "Disolver la pasta de mole en el caldo caliente.",
            ),
            favorita = false,
            creadaEn = "2026-11-20T10:00:00Z",
            esPropia = false,
        )

        val propia = Receta(
            id = "receta-propia-1",
            titulo = "Chilaquiles Rojos Caseros",
            descripcion = "Platillo tradicional mexicano con totopos crujientes en salsa roja.",
            categoria = "desayuno",
            tiempoMinutos = 25,
            porciones = 2,
            ingredientes = listOf(
                Ingrediente("Totopos de maíz", "250", "g"),
                Ingrediente("Jitomates maduros", "4", "piezas"),
                Ingrediente("Queso fresco", "100", "g"),
            ),
            pasos = listOf(
                "Hervir los jitomates hasta que la piel se desprenda.",
                "Licuar la salsa y sofreír durante cinco minutos.",
                "Bañar los totopos y servir con queso fresco.",
            ),
            favorita = true,
            creadaEn = "2026-11-24T09:00:00Z",
            esPropia = true,
        )

        recetas.addAll(listOf(comunitaria, propia))
    }

    override suspend fun listar(soloMias: Boolean): Result<List<Receta>> =
        Result.success(if (soloMias) recetas.filter { it.esPropia } else recetas.toList())

    override suspend fun obtener(id: String): Result<Receta> {
        val encontrada = recetas.find { it.id == id }
        return if (encontrada != null) {
            Result.success(encontrada)
        } else {
            Result.failure(IllegalStateException("Receta no encontrada"))
        }
    }

    override suspend fun crear(receta: Receta): Result<Receta> {
        val nuevoId = "receta-creada-${++contadorId}"
        val creada = receta.copy(
            id = nuevoId,
            creadaEn = "2026-11-26T18:00:00Z",
            esPropia = true,
        )
        recetas.add(creada)
        return Result.success(creada)
    }

    override suspend fun actualizar(receta: Receta, imagen: CambioImagen): Result<Receta> {
        val indice = recetas.indexOfFirst { it.id == receta.id }
        val actual = if (indice >= 0) recetas[indice] else null
        val resultado = when (imagen) {
            CambioImagen.Mantener -> receta.copy(
                esPropia = actual?.esPropia ?: true,
                imagenUrl = actual?.imagenUrl,
                imagenPublicId = actual?.imagenPublicId,
            )
            CambioImagen.Quitar -> receta.copy(
                esPropia = actual?.esPropia ?: true,
                imagenUrl = null,
                imagenPublicId = null,
            )
            is CambioImagen.Establecer -> receta.copy(
                esPropia = actual?.esPropia ?: true,
                imagenUrl = imagen.url,
                imagenPublicId = imagen.publicId,
            )
        }
        if (indice >= 0) {
            recetas[indice] = resultado
        }
        return Result.success(resultado)
    }

    override suspend fun eliminar(id: String): Result<Unit> {
        recetas.removeAll { it.id == id }
        _eventosSociales.tryEmit(EventoSocialReceta.RecetaEliminada(id))
        return Result.success(Unit)
    }

    override suspend fun listarFavoritos(): Result<List<Receta>> =
        Result.success(recetas.filter { it.esFavorito })

    override suspend fun darLike(recetaId: String): Result<ResultadoLike> {
        val i = recetas.indexOfFirst { it.id == recetaId }
        if (i < 0) return Result.failure(IllegalStateException("Receta no encontrada"))
        val actual = recetas[i]
        val actualizada = if (actual.leDiLike) actual else actual.copy(likesCount = actual.likesCount + 1, leDiLike = true)
        recetas[i] = actualizada
        val res = ResultadoLike(recetaId, actualizada.likesCount, actualizada.leDiLike)
        _eventosSociales.tryEmit(EventoSocialReceta.LikeActualizado(recetaId, res.likesCount, res.leDiLike))
        return Result.success(res)
    }

    override suspend fun quitarLike(recetaId: String): Result<ResultadoLike> {
        val i = recetas.indexOfFirst { it.id == recetaId }
        if (i < 0) return Result.failure(IllegalStateException("Receta no encontrada"))
        val actual = recetas[i]
        val actualizada = if (!actual.leDiLike) actual else actual.copy(likesCount = (actual.likesCount - 1).coerceAtLeast(0), leDiLike = false)
        recetas[i] = actualizada
        val res = ResultadoLike(recetaId, actualizada.likesCount, actualizada.leDiLike)
        _eventosSociales.tryEmit(EventoSocialReceta.LikeActualizado(recetaId, res.likesCount, res.leDiLike))
        return Result.success(res)
    }

    override suspend fun guardarFavorito(recetaId: String): Result<Boolean> {
        val i = recetas.indexOfFirst { it.id == recetaId }
        if (i < 0) return Result.failure(IllegalStateException("Receta no encontrada"))
        recetas[i] = recetas[i].copy(esFavorito = true)
        _eventosSociales.tryEmit(EventoSocialReceta.FavoritoActualizado(recetaId, true))
        return Result.success(true)
    }

    override suspend fun quitarFavorito(recetaId: String): Result<Boolean> {
        val i = recetas.indexOfFirst { it.id == recetaId }
        if (i < 0) return Result.failure(IllegalStateException("Receta no encontrada"))
        recetas[i] = recetas[i].copy(esFavorito = false)
        _eventosSociales.tryEmit(EventoSocialReceta.FavoritoActualizado(recetaId, false))
        return Result.success(false)
    }
}

@Singleton
class FakeAndroidImagenRepository @Inject constructor() : ImagenRepository {
    override suspend fun subir(recetaId: String, rutaArchivo: String): Result<ImagenSubida> =
        Result.success(
            ImagenSubida(
                url = "https://res.cloudinary.com/demo/image/upload/v1/recetario/recetas/$recetaId.jpg",
                publicId = "recetario/recetas/$recetaId",
            ),
        )
}

@Singleton
class FakeAndroidProcesadorImagen @Inject constructor() : ProcesadorImagen {
    override suspend fun preparar(origen: String): Result<ImagenLista> =
        Result.success(ImagenLista(ruta = "/cache/fotos/simulada.jpg", bytes = 150_000L))

    override fun descartar(ruta: String) = Unit
}

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [AuthRepositoryModule::class, RecetasRepositoryModule::class],
)
abstract class FakeTestRepositoriesModule {

    @Binds
    @Singleton
    abstract fun bindTokenProvider(impl: InMemorySessionStorage): TokenProvider

    @Binds
    @Singleton
    abstract fun bindSessionStorage(impl: InMemorySessionStorage): SessionStorage

    @Binds
    @Singleton
    abstract fun bindVisitorTokenProvider(impl: InMemoryVisitorStorage): VisitorTokenProvider

    @Binds
    @Singleton
    abstract fun bindVisitorStorage(impl: InMemoryVisitorStorage): VisitorStorage

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: FakeAndroidAuthRepository): AuthRepository

    @Binds
    @Singleton
    abstract fun bindRecetaRepository(impl: FakeAndroidRecetaRepository): RecetaRepository

    @Binds
    @Singleton
    abstract fun bindImagenRepository(impl: FakeAndroidImagenRepository): ImagenRepository

    @Binds
    @Singleton
    abstract fun bindProcesadorImagen(impl: FakeAndroidProcesadorImagen): ProcesadorImagen

    // Se conserva la implementación real RecetaTtsServiceImpl junto con AndroidTtsEngine (core/hardware/tts/di/TtsModule).
    @Binds
    @Singleton
    abstract fun bindRecetaTtsService(impl: RecetaTtsServiceImpl): RecetaTtsService
}
