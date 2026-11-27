package com.perezgomez.recetario.features.recetas.domain.repositories

import com.perezgomez.recetario.features.recetas.domain.entities.ImagenSubida

interface ImagenRepository {
    /**
     * Pide la firma a la API y sube el archivo JPEG [rutaArchivo] directo a Cloudinary.
     * La receta [recetaId] ya debe existir y ser del usuario.
     */
    suspend fun subir(recetaId: String, rutaArchivo: String): Result<ImagenSubida>
}
