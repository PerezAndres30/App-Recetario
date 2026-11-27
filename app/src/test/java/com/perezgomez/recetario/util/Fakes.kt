package com.perezgomez.recetario.util

import com.perezgomez.recetario.core.hardware.tts.domain.TtsEngine
import com.perezgomez.recetario.core.hardware.tts.domain.TtsEventListener
import com.perezgomez.recetario.features.auth.domain.entities.Usuario
import com.perezgomez.recetario.features.auth.domain.repositories.AuthRepository
import com.perezgomez.recetario.features.auth.domain.repositories.SessionStorage
import com.perezgomez.recetario.features.recetas.domain.entities.CambioImagen
import com.perezgomez.recetario.features.recetas.domain.entities.ImagenSubida
import com.perezgomez.recetario.features.recetas.domain.entities.Receta
import com.perezgomez.recetario.features.recetas.domain.imagen.ImagenLista
import com.perezgomez.recetario.features.recetas.domain.imagen.ProcesadorImagen
import com.perezgomez.recetario.features.recetas.domain.repositories.ImagenRepository
import com.perezgomez.recetario.features.recetas.domain.repositories.RecetaRepository
import com.perezgomez.recetario.features.recetas.domain.tts.EstadoLectura
import com.perezgomez.recetario.features.recetas.domain.tts.EtapaLectura
import com.perezgomez.recetario.features.recetas.domain.tts.RecetaTtsService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class FakeRecetaRepository : RecetaRepository {
    val recetas = mutableListOf<Receta>()
    var deberiaFallar: Boolean = false
    var mensajeError: String = "Error simulado en repositorio"

    var llamadasListar: Int = 0
    var llamadasCrear: Int = 0
    var llamadasActualizar: Int = 0
    var llamadasEliminar: Int = 0

    var ultimoCambioImagen: CambioImagen? = null
    var ultimoSoloMias: Boolean? = null

    override suspend fun listar(soloMias: Boolean): Result<List<Receta>> {
        llamadasListar++
        ultimoSoloMias = soloMias
        return if (deberiaFallar) Result.failure(Exception(mensajeError))
        else Result.success(if (soloMias) recetas.filter { it.esPropia } else recetas.toList())
    }

    override suspend fun obtener(id: String): Result<Receta> {
        val receta = recetas.find { it.id == id }
        return if (deberiaFallar || receta == null) {
            Result.failure(Exception(mensajeError))
        } else {
            Result.success(receta)
        }
    }

    override suspend fun crear(receta: Receta): Result<Receta> {
        llamadasCrear++
        return if (deberiaFallar) {
            Result.failure(Exception(mensajeError))
        } else {
            val creada = receta.copy(
                id = if (receta.id.isBlank()) "id-${recetas.size + 1}" else receta.id,
                esPropia = true,
            )
            recetas.add(creada)
            Result.success(creada)
        }
    }

    override suspend fun actualizar(receta: Receta, imagen: CambioImagen): Result<Receta> {
        llamadasActualizar++
        ultimoCambioImagen = imagen
        return if (deberiaFallar) {
            Result.failure(Exception(mensajeError))
        } else {
            val actual = recetas.find { it.id == receta.id }
            val resultado = when (imagen) {
                CambioImagen.Mantener -> receta.copy(
                    esPropia = actual?.esPropia ?: receta.esPropia,
                    imagenUrl = actual?.imagenUrl,
                    imagenPublicId = actual?.imagenPublicId,
                )
                CambioImagen.Quitar -> receta.copy(imagenUrl = null, imagenPublicId = null)
                is CambioImagen.Establecer -> receta.copy(imagenUrl = imagen.url, imagenPublicId = imagen.publicId)
            }
            val index = recetas.indexOfFirst { it.id == receta.id }
            if (index != -1) recetas[index] = resultado
            Result.success(resultado)
        }
    }

    override suspend fun eliminar(id: String): Result<Unit> {
        llamadasEliminar++
        return if (deberiaFallar) {
            Result.failure(Exception(mensajeError))
        } else {
            recetas.removeAll { it.id == id }
            Result.success(Unit)
        }
    }
}

class FakeImagenRepository : ImagenRepository {
    var deberiaFallar = false
    val subidas = mutableListOf<Pair<String, String>>()

    override suspend fun subir(recetaId: String, rutaArchivo: String): Result<ImagenSubida> {
        subidas.add(recetaId to rutaArchivo)
        return if (deberiaFallar) Result.failure(Exception("No se pudo subir la foto"))
        else Result.success(ImagenSubida("https://res.cloudinary.com/demo/image/upload/v1/recetario/recetas/$recetaId.jpg", "recetario/recetas/$recetaId"))
    }
}

class FakeProcesadorImagen : ProcesadorImagen {
    var deberiaFallar = false
    val descartadas = mutableListOf<String>()
    private var contador = 0

    override suspend fun preparar(origen: String): Result<ImagenLista> =
        if (deberiaFallar) Result.failure(Exception("No se pudo preparar la foto"))
        else Result.success(ImagenLista("/cache/fotos/foto-${++contador}.jpg", 200_000))

    override fun descartar(ruta: String) {
        descartadas.add(ruta)
    }
}

class FakeAuthRepository(
    private var usuario: Usuario? = Usuario("user-1", "Chef Ivan", "ivan@test.com"),
) : AuthRepository {
    override fun haySesion(): Boolean = usuario != null
    override fun usuarioActual(): Usuario? = usuario
    override suspend fun iniciarSesion(correo: String, password: String): Result<Usuario> =
        Result.success(usuario ?: Usuario("user-1", "Chef Ivan", correo))
    override suspend fun registrar(nombre: String, correo: String, password: String): Result<Usuario> =
        Result.success(Usuario("user-1", nombre, correo))
    override fun cerrarSesion() { usuario = null }
}

class FakeSessionStorage(
    private var tokenActual: String? = "token-test-123",
    private var usuarioActual: Usuario? = Usuario("user-1", "Chef Ivan", "ivan@test.com"),
) : SessionStorage {
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

class FakeTtsEngine : TtsEngine {
    private var eventListener: TtsEventListener? = null
    val textosHablados = mutableListOf<String>()
    val utteranceIds = mutableListOf<String>()
    var llamadasDetener = 0
    var llamadasLiberar = 0
    var estaHablando = false

    override fun setListener(listener: TtsEventListener?) {
        this.eventListener = listener
    }

    override fun hablar(texto: String, utteranceId: String) {
        textosHablados.add(texto)
        utteranceIds.add(utteranceId)
        estaHablando = true
        eventListener?.onInicio()
    }

    override fun detener() {
        llamadasDetener++
        estaHablando = false
        eventListener?.onFin()
    }

    override fun liberar() {
        llamadasLiberar++
        estaHablando = false
        eventListener = null
    }

    fun simularFinLocucion() {
        estaHablando = false
        eventListener?.onFin()
    }

    fun simularError(mensaje: String = "Error simulado en TTS") {
        estaHablando = false
        eventListener?.onError(mensaje)
    }
}

class FakeRecetaTtsService : RecetaTtsService {
    private val _estado = MutableStateFlow(EstadoLectura())
    override val estado: StateFlow<EstadoLectura> = _estado.asStateFlow()

    var recetaReproducida: Receta? = null
    var llamadasContinuar = 0
    var llamadasRepetir = 0
    var llamadasDetener = 0
    var llamadasLiberar = 0

    override fun reproducir(receta: Receta) {
        recetaReproducida = receta
        _estado.value = EstadoLectura(
            etapa = EtapaLectura.INTRODUCCION,
            pasoActual = 0,
            totalPasos = receta.pasos.size,
            estaReproduciendo = true,
            textoActual = "Intro: ${receta.titulo}",
        )
    }

    override fun continuar() {
        llamadasContinuar++
        val actual = _estado.value
        if (actual.etapa == EtapaLectura.INTRODUCCION) {
            if (actual.totalPasos > 0) {
                _estado.value = actual.copy(etapa = EtapaLectura.PASO, pasoActual = 1)
            } else {
                _estado.value = actual.copy(etapa = EtapaLectura.FINALIZADO)
            }
        } else if (actual.etapa == EtapaLectura.PASO) {
            if (actual.pasoActual < actual.totalPasos) {
                _estado.value = actual.copy(pasoActual = actual.pasoActual + 1)
            } else {
                _estado.value = actual.copy(etapa = EtapaLectura.FINALIZADO)
            }
        }
    }

    override fun repetir() {
        llamadasRepetir++
    }

    override fun detener() {
        llamadasDetener++
        _estado.update { it.copy(estaReproduciendo = false) }
    }

    override fun liberar() {
        llamadasLiberar++
        recetaReproducida = null
        _estado.value = EstadoLectura()
    }

    fun emitirEstado(nuevoEstado: EstadoLectura) {
        _estado.value = nuevoEstado
    }
}
