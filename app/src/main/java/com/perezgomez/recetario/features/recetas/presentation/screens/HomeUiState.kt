package com.perezgomez.recetario.features.recetas.presentation.screens

import com.perezgomez.recetario.features.recetas.domain.entities.Receta

enum class ModoHome {
    EXPLORAR,   // Catálogo público: accesible para visitantes y usuarios autenticados
    MIS_RECETAS // Espacio personal: exclusivo para usuarios autenticados
}

sealed interface ListaUi {
    data object Cargando : ListaUi
    data object Vacia : ListaUi
    data class Contenido(val recetas: List<Receta>) : ListaUi
    data class Error(val mensaje: String) : ListaUi
}

data class HomeUiState(
    val modo: ModoHome = ModoHome.EXPLORAR,
    val lista: ListaUi = ListaUi.Cargando,
    val estaAutenticado: Boolean = false,
    val nombreUsuario: String = "",
    val likesEnProceso: Set<String> = emptySet(),
    val favoritosEnProceso: Set<String> = emptySet(),
    val recetaAEliminar: Receta? = null,
    val eliminando: Boolean = false,
    val mensaje: String? = null,
    val sesionCerrada: Boolean = false,
    val sesionExpirada: Boolean = false,
    val actualizando: Boolean = false,
)
