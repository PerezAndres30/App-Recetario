package com.perezgomez.recetario.presentation.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.perezgomez.recetario.domain.repository.AuthRepository
import com.perezgomez.recetario.domain.repository.RecetaRepository
import com.perezgomez.recetario.domain.tts.RecetaTtsService
import com.perezgomez.recetario.presentation.navigation.Rutas
import com.perezgomez.recetario.presentation.screens.DetalleContenido
import com.perezgomez.recetario.presentation.screens.DetalleUiState
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
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val recetaId: String = checkNotNull(savedStateHandle.get<String>(Rutas.ARG_ID))

    private val _uiState = MutableStateFlow(
        DetalleUiState(
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
    }

    fun cargar() {
        val yaHayDatos = _uiState.value.contenido is DetalleContenido.Listo
        if (!yaHayDatos) _uiState.update { it.copy(contenido = DetalleContenido.Cargando) }

        viewModelScope.launch {
            recetaRepository.obtener(recetaId)
                .onSuccess { receta ->
                    _uiState.update {
                        it.copy(
                            contenido = DetalleContenido.Listo(receta),
                            esPropietarioVerificado = authRepository.haySesion() && receta.esPropia,
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
        if (!_uiState.value.esPropietarioVerificado || _uiState.value.eliminando) return
        _uiState.update { it.copy(eliminando = true) }
        viewModelScope.launch {
            recetaRepository.eliminar(recetaId)
                .onSuccess {
                    recetaTtsService.detener()
                    _uiState.update { it.copy(eliminando = false, confirmandoEliminar = false, eliminada = true) }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(eliminando = false, confirmandoEliminar = false, mensaje = e.message)
                    }
                }
        }
    }

    fun onMensajeMostrado() = _uiState.update { it.copy(mensaje = null) }

    override fun onCleared() {
        super.onCleared()
        recetaTtsService.detener()
    }
}
