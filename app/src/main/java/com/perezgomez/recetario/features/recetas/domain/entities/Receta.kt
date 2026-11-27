package com.perezgomez.recetario.features.recetas.domain.entities

data class Ingrediente(
    val nombre: String,
    val cantidad: String = "",
    val unidad: String = "",
)

/** Recurso principal del dominio. [id] vacío significa que todavía no existe en el servidor. */
data class Receta(
    val id: String = "",
    val titulo: String,
    val descripcion: String = "",
    val categoria: String,
    val tiempoMinutos: Int,
    val porciones: Int,
    val ingredientes: List<Ingrediente>,
    val pasos: List<String>,
    val favorita: Boolean = false,
    val creadaEn: String = "",
    /** Viene de la API: true solo si la receta es del usuario autenticado. */
    val esPropia: Boolean = false,
    /** URL https de la foto en Cloudinary; null si la receta no tiene foto. */
    val imagenUrl: String? = null,
    val imagenPublicId: String? = null,
)

/** Qué hacer con la foto de una receta al guardarla. */
sealed interface CambioImagen {
    /** No tocar la foto que ya tiene. */
    data object Mantener : CambioImagen

    /** Quitarla (la API también la borra de Cloudinary). */
    data object Quitar : CambioImagen

    data class Establecer(val url: String, val publicId: String) : CambioImagen
}

/** Resultado de subir una foto a Cloudinary. */
data class ImagenSubida(val url: String, val publicId: String)

object Categorias {
    val todas = listOf("desayuno", "comida", "cena", "postre", "bebida", "otro")
}
