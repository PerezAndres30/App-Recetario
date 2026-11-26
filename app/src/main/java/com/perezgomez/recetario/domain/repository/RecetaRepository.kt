package com.perezgomez.recetario.domain.repository

import com.perezgomez.recetario.domain.model.CambioImagen
import com.perezgomez.recetario.domain.model.Receta

interface RecetaRepository {
    /** [soloMias] = true devuelve solo las recetas del usuario autenticado (requiere sesión). */
    suspend fun listar(soloMias: Boolean = false): Result<List<Receta>>
    suspend fun obtener(id: String): Result<Receta>

    /** Crea la receta sin foto: la foto se sube después, cuando ya existe un id. */
    suspend fun crear(receta: Receta): Result<Receta>
    suspend fun actualizar(receta: Receta, imagen: CambioImagen = CambioImagen.Mantener): Result<Receta>
    suspend fun eliminar(id: String): Result<Unit>
}
