package com.perezgomez.recetario.domain.imagen

/** Foto ya lista para subir: JPEG comprimido guardado en [ruta]. */
data class ImagenLista(val ruta: String, val bytes: Long)

interface ProcesadorImagen {
    /**
     * Lee la foto de [origen] (uri de cámara o galería), corrige la rotación, la reduce a
     * [LADO_MAXIMO_PX] y la guarda como JPEG. Falla con mensaje legible si no se puede leer
     * o si queda por encima de [BYTES_MAXIMOS].
     */
    suspend fun preparar(origen: String): Result<ImagenLista>

    /** Borra un archivo temporal creado por [preparar]. */
    fun descartar(ruta: String)

    companion object {
        const val LADO_MAXIMO_PX = 1600
        const val CALIDAD_JPEG = 82
        const val BYTES_MAXIMOS = 5L * 1024 * 1024
    }
}
