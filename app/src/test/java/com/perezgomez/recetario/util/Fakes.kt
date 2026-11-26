package com.perezgomez.recetario.util

import com.perezgomez.recetario.domain.model.Receta
import com.perezgomez.recetario.domain.model.Usuario
import com.perezgomez.recetario.domain.repository.AuthRepository
import com.perezgomez.recetario.domain.repository.RecetaRepository
import com.perezgomez.recetario.domain.session.SessionStorage
import com.perezgomez.recetario.domain.tts.EstadoLectura
import com.perezgomez.recetario.domain.tts.EtapaLectura
import com.perezgomez.recetario.domain.tts.RecetaTtsService
import com.perezgomez.recetario.domain.tts.TtsEngine
import com.perezgomez.recetario.domain.tts.TtsEventListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class FakeRecetaRepository : RecetaRepository {
    val recetas = mutableListOf<Receta>()
    val recetasPropias = mutableSetOf<String>()
    var deberiaFallar: Boolean = false
    var mensajeError: String = "Error simulado en repositorio"

    var llamadasListar: Int = 0
    var llamadasCrear: Int = 0
    var llamadasActualizar: Int = 0
    var llamadasEliminar: Int = 0

    override fun esPropietario(recetaId: String): Boolean = recetasPropias.contains(recetaId)

    override fun registrarRecetaPropia(recetaId: String) {
        if (recetaId.isNotBlank()) recetasPropias.add(recetaId)
    }

    override suspend fun listar(): Result<List<Receta>> {
        llamadasListar++
        return if (deberiaFallar) Result.failure(Exception(mensajeError))
        else Result.success(recetas.toList())
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
            val creada = receta.copy(id = if (receta.id.isBlank()) "id-${recetas.size + 1}" else receta.id)
            recetas.add(creada)
            registrarRecetaPropia(creada.id)
            Result.success(creada)
        }
    }

    override suspend fun actualizar(receta: Receta): Result<Receta> {
        llamadasActualizar++
        return if (deberiaFallar) {
            Result.failure(Exception(mensajeError))
        } else {
            val index = recetas.indexOfFirst { it.id == receta.id }
            if (index != -1) recetas[index] = receta
            Result.success(receta)
        }
    }

    override suspend fun eliminar(id: String): Result<Unit> {
        llamadasEliminar++
        return if (deberiaFallar) {
            Result.failure(Exception(mensajeError))
        } else {
            recetas.removeAll { it.id == id }
            recetasPropias.remove(id)
            Result.success(Unit)
        }
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
