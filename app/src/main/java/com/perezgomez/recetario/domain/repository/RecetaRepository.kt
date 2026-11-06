package com.perezgomez.recetario.domain.repository

import com.perezgomez.recetario.domain.model.Receta

interface RecetaRepository {
    suspend fun listar(): Result<List<Receta>>
    suspend fun obtener(id: String): Result<Receta>
    suspend fun crear(receta: Receta): Result<Receta>
    suspend fun actualizar(receta: Receta): Result<Receta>
    suspend fun eliminar(id: String): Result<Unit>
}
