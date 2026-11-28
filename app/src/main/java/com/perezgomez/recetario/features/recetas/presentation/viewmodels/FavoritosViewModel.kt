package com.perezgomez.recetario.features.recetas.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.perezgomez.recetario.core.domain.error.UnauthorizedException
import com.perezgomez.recetario.features.auth.domain.repositories.AuthRepository
import com.perezgomez.recetario.features.recetas.domain.entities.EventoSocialReceta
import com.perezgomez.recetario.features.recetas.domain.entities.Receta
import com.perezgomez.recetario.features.recetas.domain.repositories.RecetaRepository
import com.perezgomez.recetario.features.recetas.domain.usecases.AlternarFavoritoUseCase
import com.perezgomez.recetario.features.recetas.domain.usecases.GetFavoritosUseCase
import com.perezgomez.recetario.features.recetas.presentation.screens.FavoritosListaUi
import com.perezgomez.recetario.features.recetas.presentation.screens.FavoritosUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class FavoritosViewModel @Inject constructor(
    private val recetaRepository: RecetaRepository,
    private val authRepository: AuthRepository,
    private val getFavoritosUseCase: GetFavoritosUseCase,
    private val alternarFavoritoUseCase: AlternarFavoritoUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        FavoritosUiState(
            nombreUsuario = authRepository.usuarioActual()?.nombre.orEmpty(),
        ),
    )
    val uiState: StateFlow<FavoritosUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            recetaRepository.eventosSociales.collect { evento ->
                aplicarEventoSocial(evento)
            }
        }
    }

    fun cargar() {
        if (!authRepository.haySesion()) {
            _uiState.update {
                it.copy(
                    lista = FavoritosListaUi.Vacia,
                    nombreUsuario = "",
                    actualizando = false,
                    sesionCerrada = true,
                )
            }
            return
        }

        val yaHayDatos = _uiState.value.lista is FavoritosListaUi.Contenido
        _uiState.update {
            it.copy(
                lista = if (yaHayDatos) it.lista else FavoritosListaUi.Cargando,
                actualizando = true,
                nombreUsuario = authRepository.usuarioActual()?.nombre.orEmpty(),
                sesionExpirada = false,
            )
        }

        viewModelScope.launch {
            getFavoritosUseCase()
                .onSuccess { recetas ->
                    _uiState.update {
                        it.copy(
                            lista = if (recetas.isEmpty()) FavoritosListaUi.Vacia else FavoritosListaUi.Contenido(recetas),
                            actualizando = false,
                        )
                    }
                }
                .onFailure { e ->
                    if (e is UnauthorizedException) {
                        manejarSesionExpirada(e.message)
                        return@onFailure
                    }
                    val texto = e.message ?: "No se pudieron cargar tus favoritos."
                    _uiState.update {
                        if (yaHayDatos) {
                            it.copy(mensaje = texto, actualizando = false)
                        } else {
                            it.copy(lista = FavoritosListaUi.Error(texto), actualizando = false)
                        }
                    }
                }
        }
    }

    fun onToggleLike(recetaId: String) {
        val actual = _uiState.value
        if (actual.likesEnProceso.contains(recetaId)) return
        val recetas = (actual.lista as? FavoritosListaUi.Contenido)?.recetas ?: return
        val previa = recetas.find { it.id == recetaId } ?: return

        val nuevoLike = !previa.leDiLike
        val nuevoConteo = if (nuevoLike) previa.likesCount + 1 else (previa.likesCount - 1).coerceAtLeast(0)

        _uiState.update { estado ->
            estado.copy(
                likesEnProceso = estado.likesEnProceso + recetaId,
                lista = actualizarRecetaEnLista(
                    estado.lista,
                    previa.copy(likesCount = nuevoConteo, leDiLike = nuevoLike),
                ),
            )
        }

        viewModelScope.launch {
            val resultado = if (nuevoLike) {
                recetaRepository.darLike(recetaId)
            } else {
                recetaRepository.quitarLike(recetaId)
            }
            resultado
                .onSuccess { confirmado ->
                    _uiState.update { estado ->
                        estado.copy(
                            likesEnProceso = estado.likesEnProceso - recetaId,
                            lista = actualizarRecetaPorId(estado.lista, recetaId) {
                                it.copy(likesCount = confirmado.likesCount, leDiLike = confirmado.leDiLike)
                            },
                        )
                    }
                }
                .onFailure { e ->
                    if (e is UnauthorizedException) {
                        _uiState.update { it.copy(likesEnProceso = it.likesEnProceso - recetaId) }
                        manejarSesionExpirada(e.message)
                        return@onFailure
                    }
                    _uiState.update { estado ->
                        estado.copy(
                            likesEnProceso = estado.likesEnProceso - recetaId,
                            lista = actualizarRecetaEnLista(estado.lista, previa),
                            mensaje = e.message ?: "No se pudo actualizar el me gusta.",
                        )
                    }
                }
        }
    }

    fun onQuitarFavorito(recetaId: String) {
        val actual = _uiState.value
        if (actual.favoritosEnProceso.contains(recetaId)) return
        val recetasPrevias = (actual.lista as? FavoritosListaUi.Contenido)?.recetas ?: return
        if (recetasPrevias.none { it.id == recetaId }) return

        val restantes = recetasPrevias.filterNot { it.id == recetaId }
        _uiState.update { estado ->
            estado.copy(
                favoritosEnProceso = estado.favoritosEnProceso + recetaId,
                lista = if (restantes.isEmpty()) FavoritosListaUi.Vacia else FavoritosListaUi.Contenido(restantes),
            )
        }

        viewModelScope.launch {
            alternarFavoritoUseCase(recetaId = recetaId, guardar = false)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            favoritosEnProceso = it.favoritosEnProceso - recetaId,
                            mensaje = "Receta quitada de favoritos",
                        )
                    }
                }
                .onFailure { e ->
                    if (e is UnauthorizedException) {
                        _uiState.update { it.copy(favoritosEnProceso = it.favoritosEnProceso - recetaId) }
                        manejarSesionExpirada(e.message)
                        return@onFailure
                    }
                    _uiState.update { estado ->
                        estado.copy(
                            favoritosEnProceso = estado.favoritosEnProceso - recetaId,
                            lista = FavoritosListaUi.Contenido(recetasPrevias),
                            mensaje = e.message ?: "No se pudo quitar de favoritos.",
                        )
                    }
                }
        }
    }

    fun onMensajeMostrado() = _uiState.update { it.copy(mensaje = null) }

    fun onCerrarSesion() {
        authRepository.cerrarSesion()
        _uiState.update {
            it.copy(
                lista = FavoritosListaUi.Vacia,
                nombreUsuario = "",
                likesEnProceso = emptySet(),
                favoritosEnProceso = emptySet(),
                sesionCerrada = true,
            )
        }
    }

    private fun aplicarEventoSocial(evento: EventoSocialReceta) {
        when (evento) {
            is EventoSocialReceta.LikeActualizado -> {
                _uiState.update { estado ->
                    estado.copy(
                        lista = actualizarRecetaPorId(estado.lista, evento.recetaId) {
                            it.copy(likesCount = evento.likesCount, leDiLike = evento.leDiLike)
                        },
                    )
                }
            }
            is EventoSocialReceta.FavoritoActualizado -> {
                if (!evento.esFavorito) {
                    _uiState.update { estado ->
                        val actual = (estado.lista as? FavoritosListaUi.Contenido)?.recetas ?: return@update estado
                        val filtradas = actual.filterNot { it.id == evento.recetaId }
                        estado.copy(
                            lista = if (filtradas.isEmpty()) FavoritosListaUi.Vacia else FavoritosListaUi.Contenido(filtradas),
                        )
                    }
                } else if (authRepository.haySesion()) {
                    cargar()
                }
            }
            is EventoSocialReceta.RecetaEliminada -> {
                _uiState.update { estado ->
                    val actual = (estado.lista as? FavoritosListaUi.Contenido)?.recetas ?: return@update estado
                    val filtradas = actual.filterNot { it.id == evento.recetaId }
                    estado.copy(
                        lista = if (filtradas.isEmpty()) FavoritosListaUi.Vacia else FavoritosListaUi.Contenido(filtradas),
                    )
                }
            }
        }
    }

    private fun manejarSesionExpirada(mensaje: String?) {
        authRepository.cerrarSesion()
        _uiState.update {
            it.copy(
                lista = FavoritosListaUi.Vacia,
                nombreUsuario = "",
                actualizando = false,
                mensaje = mensaje ?: "Tu sesión expiró. Inicia sesión de nuevo.",
                sesionExpirada = true,
            )
        }
    }

    private fun actualizarRecetaEnLista(lista: FavoritosListaUi, receta: Receta): FavoritosListaUi =
        actualizarRecetaPorId(lista, receta.id) { receta }

    private inline fun actualizarRecetaPorId(
        lista: FavoritosListaUi,
        recetaId: String,
        transformar: (Receta) -> Receta,
    ): FavoritosListaUi {
        val contenido = lista as? FavoritosListaUi.Contenido ?: return lista
        return FavoritosListaUi.Contenido(
            contenido.recetas.map { if (it.id == recetaId) transformar(it) else it },
        )
    }
}
