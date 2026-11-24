package com.perezgomez.recetario.presentation.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.perezgomez.recetario.domain.repository.AuthRepository
import com.perezgomez.recetario.domain.repository.RecetaRepository
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
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val recetaId: String = checkNotNull(savedStateHandle.get<String>(Rutas.ARG_ID))

    private val _uiState = MutableStateFlow(
        DetalleUiState(
            esPropietarioVerificado = authRepository.haySesion() && recetaRepository.esPropietario(recetaId),
        ),
    )
    val uiState: StateFlow<DetalleUiState> = _uiState.asStateFlow()

    fun cargar() {
        val yaHayDatos = _uiState.value.contenido is DetalleContenido.Listo
        val esPropia = authRepository.haySesion() && recetaRepository.esPropietario(recetaId)
        _uiState.update { it.copy(esPropietarioVerificado = esPropia) }
        if (!yaHayDatos) _uiState.update { it.copy(contenido = DetalleContenido.Cargando) }

        viewModelScope.launch {
            recetaRepository.obtener(recetaId)
                .onSuccess { receta ->
                    _uiState.update { it.copy(contenido = DetalleContenido.Listo(receta)) }
                }
                .onFailure { e ->
                    val texto = e.message ?: "No se pudo cargar la receta."
                    _uiState.update {
                        if (yaHayDatos) it.copy(mensaje = texto) else it.copy(contenido = DetalleContenido.Error(texto))
                    }
                }
        }
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
}
