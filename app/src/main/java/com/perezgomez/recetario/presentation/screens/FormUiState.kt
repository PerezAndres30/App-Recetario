package com.perezgomez.recetario.presentation.screens

data class IngredienteForm(
    val nombre: String = "",
    val cantidad: String = "",
    val unidad: String = "",
)

data class FormErrores(
    val titulo: String? = null,
    val descripcion: String? = null,
    val categoria: String? = null,
    val tiempo: String? = null,
    val porciones: String? = null,
    val ingredientes: String? = null,
    val pasos: String? = null,
) {
    fun hayErrores() = listOf(titulo, descripcion, categoria, tiempo, porciones, ingredientes, pasos).any { it != null }
}

/** Lo que se muestra en la vista previa: la foto nueva si hay, si no la que ya tenía. */
val FormUiState.vistaPreviaImagen: String? get() = imagenLocal ?: imagenUrlActual

data class FormUiState(
    val esEdicion: Boolean = false,
    val cargando: Boolean = false,
    val guardando: Boolean = false,
    val titulo: String = "",
    val descripcion: String = "",
    val categoria: String = "",
    val tiempoMinutos: String = "",
    val porciones: String = "1",
    val ingredientes: List<IngredienteForm> = listOf(IngredienteForm()),
    val pasos: List<String> = listOf(""),
    val favorita: Boolean = false,
    /** Foto que ya tiene la receta en el servidor (null si no tiene o si el usuario la quitó). */
    val imagenUrlActual: String? = null,
    /** Foto nueva ya comprimida, pendiente de subir al guardar (ruta de archivo local). */
    val imagenLocal: String? = null,
    val procesandoImagen: Boolean = false,
    val errores: FormErrores = FormErrores(),
    val error: String? = null,
    val guardado: Boolean = false,
    val mensajeExito: String = "",
)
