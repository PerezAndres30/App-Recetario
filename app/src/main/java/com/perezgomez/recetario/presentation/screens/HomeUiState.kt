package com.perezgomez.recetario.presentation.screens

import com.perezgomez.recetario.domain.model.Receta

sealed interface ListaUi {
    data object Cargando : ListaUi
    data object Vacia : ListaUi
    data class Contenido(val recetas: List<Receta>) : ListaUi
    data class Error(val mensaje: String) : ListaUi
}

data class HomeUiState(
    val lista: ListaUi = ListaUi.Cargando,
    val nombreUsuario: String = "",
    val recetaAEliminar: Receta? = null,
    val eliminando: Boolean = false,
    val mensaje: String? = null,
    val sesionCerrada: Boolean = false,
)
