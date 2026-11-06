package com.perezgomez.recetario.domain.model

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
)

object Categorias {
    val todas = listOf("desayuno", "comida", "cena", "postre", "bebida", "otro")
}
