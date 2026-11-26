package com.perezgomez.recetario.data.repository

import com.perezgomez.recetario.data.datasource.RecetaApiService
import com.perezgomez.recetario.data.mapper.toDomain
import com.perezgomez.recetario.data.mapper.toDomainList
import com.perezgomez.recetario.data.mapper.toRequestDto
import com.perezgomez.recetario.data.remote.ApiCaller
import com.perezgomez.recetario.domain.model.CambioImagen
import com.perezgomez.recetario.domain.model.Receta
import com.perezgomez.recetario.domain.repository.RecetaRepository
import javax.inject.Inject
import javax.inject.Singleton

/** La propiedad de cada receta (esPropia) la decide la API según el token; la app no la calcula. */
@Singleton
class RecetaRepositoryImpl @Inject constructor(
    private val api: RecetaApiService,
    private val apiCaller: ApiCaller,
) : RecetaRepository {

    override suspend fun listar(soloMias: Boolean): Result<List<Receta>> =
        apiCaller.call { api.listar(if (soloMias) true else null) }
            .map { respuesta -> respuesta.recetas.toDomainList() }

    override suspend fun obtener(id: String): Result<Receta> =
        apiCaller.call { api.obtener(id) }.map { it.toDomain() }

    override suspend fun crear(receta: Receta): Result<Receta> =
        apiCaller.call { api.crear(receta.toRequestDto()) }.map { it.toDomain() }

    override suspend fun actualizar(receta: Receta, imagen: CambioImagen): Result<Receta> =
        apiCaller.call { api.actualizar(receta.id, receta.toRequestDto(imagen)) }.map { it.toDomain() }

    override suspend fun eliminar(id: String): Result<Unit> =
        apiCaller.call { api.eliminar(id) }
}
