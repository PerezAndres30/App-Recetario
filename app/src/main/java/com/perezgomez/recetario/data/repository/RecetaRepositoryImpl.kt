package com.perezgomez.recetario.data.repository

import com.perezgomez.recetario.data.datasource.RecetaApiService
import com.perezgomez.recetario.data.mapper.toDomain
import com.perezgomez.recetario.data.mapper.toDomainList
import com.perezgomez.recetario.data.mapper.toRequestDto
import com.perezgomez.recetario.data.remote.ApiCaller
import com.perezgomez.recetario.domain.model.Receta
import com.perezgomez.recetario.domain.repository.RecetaRepository

class RecetaRepositoryImpl(
    private val api: RecetaApiService,
    private val apiCaller: ApiCaller,
) : RecetaRepository {

    override suspend fun listar(): Result<List<Receta>> =
        apiCaller.call { api.listar() }.map { respuesta -> respuesta.recetas.toDomainList() }

    override suspend fun obtener(id: String): Result<Receta> =
        apiCaller.call { api.obtener(id) }.map { it.toDomain() }

    override suspend fun crear(receta: Receta): Result<Receta> =
        apiCaller.call { api.crear(receta.toRequestDto()) }.map { it.toDomain() }

    override suspend fun actualizar(receta: Receta): Result<Receta> =
        apiCaller.call { api.actualizar(receta.id, receta.toRequestDto()) }.map { it.toDomain() }

    override suspend fun eliminar(id: String): Result<Unit> =
        apiCaller.call { api.eliminar(id) }
}
