package com.perezgomez.recetario.features.recetas.presentation.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.perezgomez.recetario.core.domain.error.UnauthorizedException
import com.perezgomez.recetario.core.navigation.Rutas
import com.perezgomez.recetario.features.auth.domain.repositories.AuthRepository
import com.perezgomez.recetario.features.recetas.domain.entities.EventoSocialReceta
import com.perezgomez.recetario.features.recetas.domain.repositories.RecetaRepository
import com.perezgomez.recetario.features.recetas.domain.tts.RecetaTtsService
import com.perezgomez.recetario.features.recetas.domain.usecases.AlternarFavoritoUseCase
import com.perezgomez.recetario.features.recetas.domain.usecases.EliminarRecetaPropiaUseCase
import com.perezgomez.recetario.features.recetas.presentation.screens.DetalleContenido
import com.perezgomez.recetario.features.recetas.presentation.screens.DetalleUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class DetalleViewModel @Inject constructor(
    private val recetaRepository: RecetaRepository,
    private val authRepository: AuthRepository,
    private val recetaTtsService: RecetaTtsService,
    private val eliminarRecetaPropiaUseCase: EliminarRecetaPropiaUseCase,
    private val alternarFavoritoUseCase: AlternarFavoritoUseCase,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val recetaId: String = checkNotNull(savedStateHandle.get<String>(Rutas.ARG_ID))

    private val _uiState = MutableStateFlow(
        DetalleUiState(
            estaAutenticado = authRepository.haySesion(),
            ttsState = recetaTtsService.estado.value,
        ),
    )
    val uiState: StateFlow<DetalleUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            recetaTtsService.estado.collect { estadoTts ->
                _uiState.update { it.copy(ttsState = estadoTts) }
            }
        }
        viewModelScope.launch {
            recetaRepository.eventosSociales.collect { evento ->
                aplicarEventoSocial(evento)
            }
        }
    }

    fun cargar() {
        val autenticado = authRepository.haySesion()
        val yaHayDatos = _uiState.value.contenido is DetalleContenido.Listo
        _uiState.update {
            it.copy(
                contenido = if (yaHayDatos) it.contenido else DetalleContenido.Cargando,
                estaAutenticado = autenticado,
                sesionExpirada = false,
            )
        }

        viewModelScope.launch {
            recetaRepository.obtener(recetaId)
                .onSuccess { receta ->
                    val sesionActiva = authRepository.haySesion()
                    _uiState.update {
                        it.copy(
                            contenido = DetalleContenido.Listo(receta),
                            estaAutenticado = sesionActiva,
                            esPropietarioVerificado = sesionActiva && receta.esPropia,
                        )
                    }
                }
                .onFailure { e ->
                    val texto = e.message ?: "No se pudo cargar la receta."
                    _uiState.update {
                        if (yaHayDatos) it.copy(mensaje = texto) else it.copy(contenido = DetalleContenido.Error(texto))
                    }
                }
        }
    }

    fun onToggleLike() {
        val actual = _uiState.value
        if (actual.procesandoLike) return
        val previa = (actual.contenido as? DetalleContenido.Listo)?.receta ?: return

        val nuevoLike = !previa.leDiLike
        val nuevoConteo = if (nuevoLike) previa.likesCount + 1 else (previa.likesCount - 1).coerceAtLeast(0)

        _uiState.update {
            it.copy(
                procesandoLike = true,
                contenido = DetalleContenido.Listo(previa.copy(likesCount = nuevoConteo, leDiLike = nuevoLike)),
            )
        }

        viewModelScope.launch {
            val res = if (nuevoLike) recetaRepository.darLike(recetaId) else recetaRepository.quitarLike(recetaId)
            res
                .onSuccess { confirmado ->
                    _uiState.update { estado ->
                        val recetaActual = (estado.contenido as? DetalleContenido.Listo)?.receta ?: previa
                        estado.copy(
                            procesandoLike = false,
                            contenido = DetalleContenido.Listo(
                                recetaActual.copy(
                                    likesCount = confirmado.likesCount,
                                    leDiLike = confirmado.leDiLike,
                                ),
                            ),
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            procesandoLike = false,
                            contenido = DetalleContenido.Listo(previa),
                        )
                    }
                    if (e is UnauthorizedException) {
                        manejarSesionExpirada(e.message)
                    } else {
                        _uiState.update { it.copy(mensaje = e.message ?: "No se pudo actualizar el me gusta.") }
                    }
                }
        }
    }

    fun onToggleFavorito() {
        val actual = _uiState.value
        if (actual.procesandoFavorito) return
        if (!authRepository.haySesion()) {
            _uiState.update { it.copy(mensaje = "Inicia sesión para guardar recetas en favoritos.") }
            return
        }
        val previa = (actual.contenido as? DetalleContenido.Listo)?.receta ?: return
        val nuevoFavorito = !previa.esFavorito

        _uiState.update {
            it.copy(
                procesandoFavorito = true,
                contenido = DetalleContenido.Listo(previa.copy(esFavorito = nuevoFavorito)),
            )
        }

        viewModelScope.launch {
            alternarFavoritoUseCase(recetaId = recetaId, guardar = nuevoFavorito)
                .onSuccess { confirmado ->
                    _uiState.update { estado ->
                        val recetaActual = (estado.contenido as? DetalleContenido.Listo)?.receta ?: previa
                        estado.copy(
                            procesandoFavorito = false,
                            contenido = DetalleContenido.Listo(recetaActual.copy(esFavorito = confirmado)),
                            mensaje = if (confirmado) "Guardada en favoritos" else "Quitada de favoritos",
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            procesandoFavorito = false,
                            contenido = DetalleContenido.Listo(previa),
                        )
                    }
                    if (e is UnauthorizedException) {
                        manejarSesionExpirada(e.message)
                    } else {
                        _uiState.update { it.copy(mensaje = e.message ?: "No se pudo actualizar favoritos.") }
                    }
                }
        }
    }

    /** Inicia la lectura en voz alta de la receta (título, descripción e ingredientes). */
    fun onReproducirTts() {
        val receta = (_uiState.value.contenido as? DetalleContenido.Listo)?.receta ?: return
        recetaTtsService.reproducir(receta)
    }

    /** Avanza al siguiente paso de preparación o anuncia la finalización. */
    fun onContinuarTts() {
        recetaTtsService.continuar()
    }

    /** Vuelve a escuchar la introducción o el paso actual. */
    fun onRepetirTts() {
        recetaTtsService.repetir()
    }

    /** Detiene de inmediato la locución activa (por ejemplo, al salir de la pantalla). */
    fun detenerLectura() {
        recetaTtsService.detener()
    }

    fun onEliminarClick() {
        if (!_uiState.value.esPropietarioVerificado) return
        _uiState.update { it.copy(confirmandoEliminar = true) }
    }

    fun onCancelarEliminar() = _uiState.update { it.copy(confirmandoEliminar = false) }

    fun onConfirmarEliminar() {
        if (_uiState.value.eliminando) return
        val esPropia = (_uiState.value.contenido as? DetalleContenido.Listo)?.receta?.esPropia == true
        _uiState.update { it.copy(eliminando = true) }
        viewModelScope.launch {
            eliminarRecetaPropiaUseCase(recetaId, esPropia)
                .onSuccess {
                    recetaTtsService.detener()
                    _uiState.update { it.copy(eliminando = false, confirmandoEliminar = false, eliminada = true) }
                }
                .onFailure { e ->
                    if (e is UnauthorizedException) {
                        _uiState.update { it.copy(eliminando = false, confirmandoEliminar = false) }
                        manejarSesionExpirada(e.message)
                    } else {
                        _uiState.update {
                            it.copy(eliminando = false, confirmandoEliminar = false, mensaje = e.message)
                        }
                    }
                }
        }
    }

    fun onMensajeMostrado() = _uiState.update { it.copy(mensaje = null) }

    private fun aplicarEventoSocial(evento: EventoSocialReceta) {
        val actual = (_uiState.value.contenido as? DetalleContenido.Listo)?.receta ?: return
        when (evento) {
            is EventoSocialReceta.LikeActualizado -> if (evento.recetaId == actual.id) {
                _uiState.update {
                    it.copy(
                        contenido = DetalleContenido.Listo(
                            actual.copy(likesCount = evento.likesCount, leDiLike = evento.leDiLike),
                        ),
                    )
                }
            }
            is EventoSocialReceta.FavoritoActualizado -> if (evento.recetaId == actual.id) {
                _uiState.update {
                    it.copy(contenido = DetalleContenido.Listo(actual.copy(esFavorito = evento.esFavorito)))
                }
            }
            is EventoSocialReceta.RecetaEliminada -> Unit
        }
    }

    private fun manejarSesionExpirada(mensaje: String?) {
        authRepository.cerrarSesion()
        _uiState.update { estado ->
            val listaLimpiada = (estado.contenido as? DetalleContenido.Listo)?.let {
                DetalleContenido.Listo(it.receta.copy(esPropia = false, esFavorito = false))
            } ?: estado.contenido
            estado.copy(
                contenido = listaLimpiada,
                estaAutenticado = false,
                esPropietarioVerificado = false,
                mensaje = mensaje ?: "Tu sesión expiró. Inicia sesión de nuevo.",
                sesionExpirada = true,
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        recetaTtsService.detener()
    }
}
