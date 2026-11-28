package com.perezgomez.recetario.features.recetas.domain.repositories

import com.perezgomez.recetario.features.recetas.domain.entities.CambioImagen
import com.perezgomez.recetario.features.recetas.domain.entities.EventoSocialReceta
import com.perezgomez.recetario.features.recetas.domain.entities.Receta
import com.perezgomez.recetario.features.recetas.domain.entities.ResultadoLike
import kotlinx.coroutines.flow.Flow

interface RecetaRepository {
    val eventosSociales: Flow<EventoSocialReceta>

    /** [soloMias] = true devuelve solo las recetas del usuario autenticado (requiere sesión). */
    suspend fun listar(soloMias: Boolean = false): Result<List<Receta>>
    suspend fun obtener(id: String): Result<Receta>

    /** Crea la receta sin foto: la foto se sube después, cuando ya existe un id. */
    suspend fun crear(receta: Receta): Result<Receta>
    suspend fun actualizar(receta: Receta, imagen: CambioImagen = CambioImagen.Mantener): Result<Receta>
    suspend fun eliminar(id: String): Result<Unit>

    suspend fun listarFavoritos(): Result<List<Receta>>
    suspend fun darLike(recetaId: String): Result<ResultadoLike>
    suspend fun quitarLike(recetaId: String): Result<ResultadoLike>
    suspend fun guardarFavorito(recetaId: String): Result<Boolean>
    suspend fun quitarFavorito(recetaId: String): Result<Boolean>
}
