package com.perezgomez.recetario.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.perezgomez.recetario.domain.model.Receta
import com.perezgomez.recetario.domain.repository.AuthRepository
import com.perezgomez.recetario.domain.repository.RecetaRepository
import com.perezgomez.recetario.presentation.screens.HomeUiState
import com.perezgomez.recetario.presentation.screens.ListaUi
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val recetaRepository: RecetaRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        HomeUiState(nombreUsuario = authRepository.usuarioActual()?.nombre.orEmpty()),
    )
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    /** Se llama cada vez que la pantalla vuelve a primer plano, para reflejar cambios hechos en otras pantallas. */
    fun cargar() {
        val yaHayDatos = _uiState.value.lista is ListaUi.Contenido
        _uiState.update {
            it.copy(
                lista = if (yaHayDatos) it.lista else ListaUi.Cargando,
                actualizando = true,
            )
        }

        viewModelScope.launch {
            recetaRepository.listar()
                .onSuccess { recetas ->
                    _uiState.update {
                        it.copy(
                            lista = if (recetas.isEmpty()) ListaUi.Vacia else ListaUi.Contenido(recetas),
                            actualizando = false,
                        )
                    }
                }
                .onFailure { e ->
                    val texto = e.message ?: "No se pudieron cargar las recetas."
                    _uiState.update {
                        if (yaHayDatos) {
                            it.copy(mensaje = texto, actualizando = false)
                        } else {
                            it.copy(lista = ListaUi.Error(texto), actualizando = false)
                        }
                    }
                }
        }
    }

    fun onEliminarClick(receta: Receta) = _uiState.update { it.copy(recetaAEliminar = receta) }

    fun onCancelarEliminar() = _uiState.update { it.copy(recetaAEliminar = null) }

    fun onConfirmarEliminar() {
        val receta = _uiState.value.recetaAEliminar ?: return
        if (_uiState.value.eliminando) return

        _uiState.update { it.copy(eliminando = true) }
        viewModelScope.launch {
            recetaRepository.eliminar(receta.id)
                .onSuccess {
                    _uiState.update { estado ->
                        val restantes = (estado.lista as? ListaUi.Contenido)?.recetas
                            .orEmpty()
                            .filterNot { it.id == receta.id }
                        estado.copy(
                            lista = if (restantes.isEmpty()) ListaUi.Vacia else ListaUi.Contenido(restantes),
                            recetaAEliminar = null,
                            eliminando = false,
                            mensaje = "Receta eliminada",
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(recetaAEliminar = null, eliminando = false, mensaje = e.message)
                    }
                }
        }
    }

    fun onMensajeMostrado() = _uiState.update { it.copy(mensaje = null) }

    fun onCerrarSesion() {
        authRepository.cerrarSesion()
        _uiState.update { it.copy(sesionCerrada = true) }
    }
}
