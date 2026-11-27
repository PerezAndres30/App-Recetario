package com.perezgomez.recetario.features.recetas.data.repositories

import com.perezgomez.recetario.core.network.ApiCaller
import com.perezgomez.recetario.features.auth.domain.repositories.SessionStorage
import com.perezgomez.recetario.features.recetas.data.datasources.remote.api.RecetaApiService
import com.perezgomez.recetario.features.recetas.data.datasources.remote.mapper.toDomain
import com.perezgomez.recetario.features.recetas.data.datasources.remote.mapper.toDomainList
import com.perezgomez.recetario.features.recetas.data.datasources.remote.mapper.toRequestDto
import com.perezgomez.recetario.features.recetas.domain.entities.Receta
import com.perezgomez.recetario.features.recetas.domain.repositories.RecetaRepository
import java.util.Collections
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecetaRepositoryImpl @Inject constructor(
    private val api: RecetaApiService,
    private val apiCaller: ApiCaller,
    private val session: SessionStorage,
) : RecetaRepository {

    /**
     * Mapea el identificador de cada usuario autenticado con el conjunto de IDs
     * de recetas cuya autoría fue registrada durante su sesión en este dispositivo.
     * De este modo, la propiedad queda estrictamente aislada por cuenta y no se
     * filtran privilegios de edición o eliminación entre distintos usuarios.
     */
    private val recetasPorUsuario = ConcurrentHashMap<String, MutableSet<String>>()

    override fun esPropietario(recetaId: String): Boolean {
        val usuarioId = session.usuario()?.id ?: return false
        return recetasPorUsuario[usuarioId]?.contains(recetaId) == true
    }

    override fun registrarRecetaPropia(recetaId: String) {
        val usuarioId = session.usuario()?.id ?: return
        if (recetaId.isNotBlank()) {
            recetasPorUsuario.computeIfAbsent(usuarioId) { Collections.synchronizedSet(mutableSetOf()) }
                .add(recetaId)
        }
    }

    override suspend fun listar(): Result<List<Receta>> =
        apiCaller.call { api.listar() }.map { respuesta -> respuesta.recetas.toDomainList() }

    override suspend fun obtener(id: String): Result<Receta> =
        apiCaller.call { api.obtener(id) }.map { it.toDomain() }

    override suspend fun crear(receta: Receta): Result<Receta> =
        apiCaller.call { api.crear(receta.toRequestDto()) }.map { dto ->
            val creada = dto.toDomain()
            registrarRecetaPropia(creada.id)
            creada
        }

    override suspend fun actualizar(receta: Receta): Result<Receta> =
        apiCaller.call { api.actualizar(receta.id, receta.toRequestDto()) }.map { it.toDomain() }

    override suspend fun eliminar(id: String): Result<Unit> =
        apiCaller.call { api.eliminar(id) }.also { resultado ->
            if (resultado.isSuccess) {
                val usuarioId = session.usuario()?.id
                if (usuarioId != null) {
                    recetasPorUsuario[usuarioId]?.remove(id)
                }
            }
        }
}
