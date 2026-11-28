package com.perezgomez.recetario.core.navigation

object Rutas {
    const val BIENVENIDA = "bienvenida"
    const val LOGIN = "login"
    const val REGISTRO = "registro"
    const val EXPLORAR = "explorar"
    const val HOME = "home"
    const val FAVORITOS = "favoritos"
    const val DETALLE = "detalle/{id}"
    const val FORMULARIO = "formulario?id={id}"

    /** Nombre del argumento de ruta que lleva el id de la receta. */
    const val ARG_ID = "id"

    /** Clave para pasar un mensaje (Snackbar) a la pantalla anterior al volver. */
    const val MENSAJE_NAV = "mensaje"

    fun detalle(id: String) = "detalle/$id"
    fun formulario(id: String? = null) = if (id == null) "formulario" else "formulario?id=$id"
}
