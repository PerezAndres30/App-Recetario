package com.perezgomez.recetario.features.recetas.domain.repositories

import com.perezgomez.recetario.features.recetas.domain.entities.Receta

interface RecetaRepository {
    suspend fun listar(): Result<List<Receta>>
    suspend fun obtener(id: String): Result<Receta>
    suspend fun crear(receta: Receta): Result<Receta>
    suspend fun actualizar(receta: Receta): Result<Receta>
    suspend fun eliminar(id: String): Result<Unit>

    /**
     * Verifica si una receta fue creada por el usuario autenticado en la aplicación.
     * Mientras el backend no suministre el ID de propietario en la API,
     * permite discriminar recetas propias verificadas de la lista global comunitaria.
     */
    fun esPropietario(recetaId: String): Boolean
    fun registrarRecetaPropia(recetaId: String)
}
