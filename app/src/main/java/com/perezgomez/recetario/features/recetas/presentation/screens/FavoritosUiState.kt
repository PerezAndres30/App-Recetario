package com.perezgomez.recetario.features.recetas.presentation.screens

import com.perezgomez.recetario.features.recetas.domain.entities.Receta

sealed interface FavoritosListaUi {
    data object Cargando : FavoritosListaUi
    data object Vacia : FavoritosListaUi
    data class Contenido(val recetas: List<Receta>) : FavoritosListaUi
    data class Error(val mensaje: String) : FavoritosListaUi
}

data class FavoritosUiState(
    val lista: FavoritosListaUi = FavoritosListaUi.Cargando,
    val nombreUsuario: String = "",
    val actualizando: Boolean = false,
    val likesEnProceso: Set<String> = emptySet(),
    val favoritosEnProceso: Set<String> = emptySet(),
    val mensaje: String? = null,
    val sesionCerrada: Boolean = false,
    val sesionExpirada: Boolean = false,
)
