package com.perezgomez.recetario.presentation.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.perezgomez.recetario.domain.repository.RecetaRepository
import com.perezgomez.recetario.presentation.navigation.Rutas
import com.perezgomez.recetario.presentation.screens.DetalleContenido
import com.perezgomez.recetario.presentation.screens.DetalleUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DetalleViewModel(
    private val recetaRepository: RecetaRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val recetaId: String = checkNotNull(savedStateHandle.get<String>(Rutas.ARG_ID))

    private val _uiState = MutableStateFlow(DetalleUiState())
    val uiState: StateFlow<DetalleUiState> = _uiState.asStateFlow()

    /** Se llama al volver a primer plano, así se refleja una edición recién guardada. */
    fun cargar() {
        val yaHayDatos = _uiState.value.contenido is DetalleContenido.Listo
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

    fun onMensajeMostrado() = _uiState.update { it.copy(mensaje = null) }
}
