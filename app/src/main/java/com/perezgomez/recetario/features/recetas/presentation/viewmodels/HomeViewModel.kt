package com.perezgomez.recetario.features.recetas.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.perezgomez.recetario.core.domain.error.UnauthorizedException
import com.perezgomez.recetario.features.auth.domain.repositories.AuthRepository
import com.perezgomez.recetario.features.recetas.domain.entities.EventoSocialReceta
import com.perezgomez.recetario.features.recetas.domain.entities.Receta
import com.perezgomez.recetario.features.recetas.domain.repositories.RecetaRepository
import com.perezgomez.recetario.features.recetas.domain.usecases.AlternarFavoritoUseCase
import com.perezgomez.recetario.features.recetas.domain.usecases.EliminarRecetaPropiaUseCase
import com.perezgomez.recetario.features.recetas.domain.usecases.GetMisRecetasUseCase
import com.perezgomez.recetario.features.recetas.presentation.screens.HomeUiState
import com.perezgomez.recetario.features.recetas.presentation.screens.ListaUi
import com.perezgomez.recetario.features.recetas.presentation.screens.ModoHome
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
    private val getMisRecetasUseCase: GetMisRecetasUseCase,
    private val eliminarRecetaPropiaUseCase: EliminarRecetaPropiaUseCase,
    private val alternarFavoritoUseCase: AlternarFavoritoUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        HomeUiState(
            estaAutenticado = authRepository.haySesion(),
            nombreUsuario = authRepository.usuarioActual()?.nombre.orEmpty(),
            modo = if (authRepository.haySesion()) ModoHome.MIS_RECETAS else ModoHome.EXPLORAR,
        ),
    )
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            recetaRepository.eventosSociales.collect { evento ->
                aplicarEventoSocial(evento)
            }
        }
    }

    fun configurarModo(modo: ModoHome) {
        val autenticado = authRepository.haySesion()
        _uiState.update {
            it.copy(
                modo = modo,
                estaAutenticado = autenticado,
                nombreUsuario = if (autenticado) authRepository.usuarioActual()?.nombre.orEmpty() else "",
            )
        }
    }

    /** Se llama cada vez que la pantalla vuelve a primer plano, para reflejar cambios hechos en otras pantallas. */
    fun cargar() {
        val autenticado = authRepository.haySesion()
        val yaHayDatos = _uiState.value.lista is ListaUi.Contenido
        _uiState.update {
            it.copy(
                lista = if (yaHayDatos) it.lista else ListaUi.Cargando,
                actualizando = true,
                estaAutenticado = autenticado,
                nombreUsuario = if (autenticado) authRepository.usuarioActual()?.nombre.orEmpty() else "",
                sesionExpirada = false,
            )
        }

        viewModelScope.launch {
            // "Mis recetas" consulta ?mias=true vía UseCase; el catálogo es público.
            val resultado = when (_uiState.value.modo) {
                ModoHome.EXPLORAR -> recetaRepository.listar(soloMias = false)
                ModoHome.MIS_RECETAS -> getMisRecetasUseCase()
            }
            resultado
                .onSuccess { recetas ->
                    _uiState.update { estado ->
                        estado.copy(
                            lista = if (recetas.isEmpty()) ListaUi.Vacia else ListaUi.Contenido(recetas),
                            actualizando = false,
                        )
                    }
                }
                .onFailure { e ->
                    if (e is UnauthorizedException) {
                        manejarSesionExpirada(e.message)
                        return@onFailure
                    }
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

    fun onToggleLike(recetaId: String) {
        val actual = _uiState.value
        if (actual.likesEnProceso.contains(recetaId)) return
        val contenido = actual.lista as? ListaUi.Contenido ?: return
        val previa = contenido.recetas.find { it.id == recetaId } ?: return

        val nuevoLike = !previa.leDiLike
        val nuevoConteo = if (nuevoLike) previa.likesCount + 1 else (previa.likesCount - 1).coerceAtLeast(0)

        _uiState.update { estado ->
            estado.copy(
                likesEnProceso = estado.likesEnProceso + recetaId,
                lista = actualizarReceta(
                    estado.lista,
                    previa.copy(likesCount = nuevoConteo, leDiLike = nuevoLike),
                ),
            )
        }

        viewModelScope.launch {
            val res = if (nuevoLike) recetaRepository.darLike(recetaId) else recetaRepository.quitarLike(recetaId)
            res
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
                    _uiState.update { estado ->
                        estado.copy(
                            likesEnProceso = estado.likesEnProceso - recetaId,
                            lista = actualizarReceta(estado.lista, previa),
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

    fun onToggleFavorito(recetaId: String) {
        val actual = _uiState.value
        if (actual.favoritosEnProceso.contains(recetaId)) return
        if (!authRepository.haySesion()) {
            _uiState.update { it.copy(mensaje = "Inicia sesión para guardar recetas en favoritos.") }
            return
        }

        val contenido = actual.lista as? ListaUi.Contenido ?: return
        val previa = contenido.recetas.find { it.id == recetaId } ?: return
        val nuevoFavorito = !previa.esFavorito

        _uiState.update { estado ->
            estado.copy(
                favoritosEnProceso = estado.favoritosEnProceso + recetaId,
                lista = actualizarReceta(estado.lista, previa.copy(esFavorito = nuevoFavorito)),
            )
        }

        viewModelScope.launch {
            alternarFavoritoUseCase(recetaId = recetaId, guardar = nuevoFavorito)
                .onSuccess { confirmado ->
                    _uiState.update { estado ->
                        estado.copy(
                            favoritosEnProceso = estado.favoritosEnProceso - recetaId,
                            lista = actualizarRecetaPorId(estado.lista, recetaId) {
                                it.copy(esFavorito = confirmado)
                            },
                            mensaje = if (confirmado) "Guardada en favoritos" else "Quitada de favoritos",
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update { estado ->
                        estado.copy(
                            favoritosEnProceso = estado.favoritosEnProceso - recetaId,
                            lista = actualizarReceta(estado.lista, previa),
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

    fun onEliminarClick(receta: Receta) {
        // Solo el creador puede eliminar (la API lo marca con esPropia y además lo exige con 403)
        if (!authRepository.haySesion() || !receta.esPropia) return
        _uiState.update { it.copy(recetaAEliminar = receta) }
    }

    fun onCancelarEliminar() = _uiState.update { it.copy(recetaAEliminar = null) }

    fun onConfirmarEliminar() {
        val receta = _uiState.value.recetaAEliminar ?: return
        if (!authRepository.haySesion() || !receta.esPropia) {
            _uiState.update { it.copy(recetaAEliminar = null) }
            return
        }
        if (_uiState.value.eliminando) return

        _uiState.update { it.copy(eliminando = true) }
        viewModelScope.launch {
            eliminarRecetaPropiaUseCase(receta.id, receta.esPropia)
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
                    if (e is UnauthorizedException) {
                        _uiState.update { it.copy(recetaAEliminar = null, eliminando = false) }
                        manejarSesionExpirada(e.message)
                    } else {
                        _uiState.update {
                            it.copy(recetaAEliminar = null, eliminando = false, mensaje = e.message)
                        }
                    }
                }
        }
    }

    fun onMensajeMostrado() = _uiState.update { it.copy(mensaje = null) }

    fun onCerrarSesion() {
        authRepository.cerrarSesion()
        _uiState.update { estado ->
            val listaPublicaLimpia = when {
                estado.modo == ModoHome.MIS_RECETAS -> ListaUi.Vacia
                estado.lista is ListaUi.Contenido -> ListaUi.Contenido(
                    estado.lista.recetas.map { it.copy(esPropia = false, esFavorito = false) },
                )
                else -> estado.lista
            }
            estado.copy(
                sesionCerrada = true,
                estaAutenticado = false,
                nombreUsuario = "",
                modo = ModoHome.EXPLORAR,
                lista = listaPublicaLimpia,
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
                _uiState.update { estado ->
                    estado.copy(
                        lista = actualizarRecetaPorId(estado.lista, evento.recetaId) {
                            it.copy(esFavorito = evento.esFavorito)
                        },
                    )
                }
            }
            is EventoSocialReceta.RecetaEliminada -> {
                _uiState.update { estado ->
                    val actual = (estado.lista as? ListaUi.Contenido)?.recetas ?: return@update estado
                    val restantes = actual.filterNot { it.id == evento.recetaId }
                    estado.copy(
                        lista = if (restantes.isEmpty()) ListaUi.Vacia else ListaUi.Contenido(restantes),
                    )
                }
            }
        }
    }

    private fun manejarSesionExpirada(mensaje: String?) {
        authRepository.cerrarSesion()
        _uiState.update { estado ->
            val listaSinPrivados = when {
                estado.modo == ModoHome.MIS_RECETAS -> ListaUi.Vacia
                estado.lista is ListaUi.Contenido -> ListaUi.Contenido(
                    estado.lista.recetas.map { it.copy(esPropia = false, esFavorito = false) },
                )
                else -> estado.lista
            }
            estado.copy(
                estaAutenticado = false,
                nombreUsuario = "",
                actualizando = false,
                lista = listaSinPrivados,
                mensaje = mensaje ?: "Tu sesión expiró. Inicia sesión de nuevo.",
                sesionExpirada = true,
            )
        }
    }

    private fun actualizarReceta(lista: ListaUi, receta: Receta): ListaUi =
        actualizarRecetaPorId(lista, receta.id) { receta }

    private inline fun actualizarRecetaPorId(
        lista: ListaUi,
        recetaId: String,
        transformar: (Receta) -> Receta,
    ): ListaUi {
        val contenido = lista as? ListaUi.Contenido ?: return lista
        return ListaUi.Contenido(
            contenido.recetas.map { if (it.id == recetaId) transformar(it) else it },
        )
    }
}
