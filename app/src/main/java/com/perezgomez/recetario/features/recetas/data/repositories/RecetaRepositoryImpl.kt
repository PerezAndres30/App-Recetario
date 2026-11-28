package com.perezgomez.recetario.features.recetas.data.repositories

import com.perezgomez.recetario.core.network.ApiCaller
import com.perezgomez.recetario.core.network.TokenProvider
import com.perezgomez.recetario.features.auth.data.repositories.VisitorIdentityManager
import com.perezgomez.recetario.features.recetas.data.datasources.remote.api.RecetaApiService
import com.perezgomez.recetario.features.recetas.data.datasources.remote.mapper.toDomain
import com.perezgomez.recetario.features.recetas.data.datasources.remote.mapper.toDomainList
import com.perezgomez.recetario.features.recetas.data.datasources.remote.mapper.toRequestDto
import com.perezgomez.recetario.features.recetas.domain.entities.CambioImagen
import com.perezgomez.recetario.features.recetas.domain.entities.EventoSocialReceta
import com.perezgomez.recetario.features.recetas.domain.entities.Receta
import com.perezgomez.recetario.features.recetas.domain.entities.ResultadoLike
import com.perezgomez.recetario.features.recetas.domain.repositories.RecetaRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Fuente única de verdad remota para recetas, likes y favoritos.
 * Cuando no hay sesión activa y el usuario realiza una mutación de like, asegura previamente
 * la identidad anónima firmada mediante [VisitorIdentityManager].
 */
@Singleton
class RecetaRepositoryImpl @Inject constructor(
    private val api: RecetaApiService,
    private val tokenProvider: TokenProvider,
    private val visitorIdentityManager: VisitorIdentityManager,
    private val apiCaller: ApiCaller,
) : RecetaRepository {

    private val _eventosSociales = MutableSharedFlow<EventoSocialReceta>(extraBufferCapacity = 64)
    override val eventosSociales: Flow<EventoSocialReceta> = _eventosSociales.asSharedFlow()

    override suspend fun listar(soloMias: Boolean): Result<List<Receta>> =
        apiCaller.call { api.listar(if (soloMias) true else null) }
            .map { respuesta -> respuesta.recetas.toDomainList() }

    override suspend fun obtener(id: String): Result<Receta> =
        apiCaller.call { api.obtener(id) }.map { it.toDomain() }

    override suspend fun crear(receta: Receta): Result<Receta> =
        apiCaller.call { api.crear(receta.toRequestDto(incluirFavorita = false)) }.map { it.toDomain() }

    override suspend fun actualizar(receta: Receta, imagen: CambioImagen): Result<Receta> =
        apiCaller.call {
            api.actualizar(receta.id, receta.toRequestDto(imagen = imagen, incluirFavorita = false))
        }.map { it.toDomain() }

    override suspend fun eliminar(id: String): Result<Unit> =
        apiCaller.call { api.eliminar(id) }.onSuccess {
            _eventosSociales.tryEmit(EventoSocialReceta.RecetaEliminada(id))
        }

    override suspend fun listarFavoritos(): Result<List<Receta>> =
        apiCaller.call { api.listarFavoritos() }
            .map { respuesta -> respuesta.recetas.toDomainList() }

    override suspend fun darLike(recetaId: String): Result<ResultadoLike> =
        ejecutarAccionLike(recetaId) { api.darLike(recetaId).toDomain() }

    override suspend fun quitarLike(recetaId: String): Result<ResultadoLike> =
        ejecutarAccionLike(recetaId) { api.quitarLike(recetaId).toDomain() }

    override suspend fun guardarFavorito(recetaId: String): Result<Boolean> =
        apiCaller.call { api.guardarFavorito(recetaId).esFavorito }
            .onSuccess { esFav ->
                _eventosSociales.tryEmit(EventoSocialReceta.FavoritoActualizado(recetaId, esFav))
            }

    override suspend fun quitarFavorito(recetaId: String): Result<Boolean> =
        apiCaller.call { api.quitarFavorito(recetaId).esFavorito }
            .onSuccess { esFav ->
                _eventosSociales.tryEmit(EventoSocialReceta.FavoritoActualizado(recetaId, esFav))
            }

    private suspend fun ejecutarAccionLike(
        recetaId: String,
        llamada: suspend () -> ResultadoLike,
    ): Result<ResultadoLike> {
        if (tokenProvider.token().isNullOrBlank()) {
            visitorIdentityManager.asegurarTokenVisitante().getOrElse { error ->
                return Result.failure(error)
            }
        }
        return apiCaller.call { llamada() }.onSuccess { resultado ->
            _eventosSociales.tryEmit(
                EventoSocialReceta.LikeActualizado(
                    recetaId = resultado.recetaId,
                    likesCount = resultado.likesCount,
                    leDiLike = resultado.leDiLike,
                ),
            )
        }
    }
}
