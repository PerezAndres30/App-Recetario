package com.perezgomez.recetario.features.auth.data.repositories

import com.perezgomez.recetario.core.network.ApiCaller
import com.perezgomez.recetario.features.auth.data.datasources.remote.api.AuthApiService
import com.perezgomez.recetario.features.auth.domain.repositories.VisitorStorage
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Coordina la obtención y reutilización de la identidad anónima firmada por el servidor.
 * Usa un [Mutex] para garantizar que solicitudes concurrentes no disparen llamadas duplicadas
 * a `POST /visitante/sesion`.
 */
@Singleton
class VisitorIdentityManager @Inject constructor(
    private val api: AuthApiService,
    private val visitorStorage: VisitorStorage,
    private val apiCaller: ApiCaller,
) {
    private val mutex = Mutex()

    suspend fun asegurarTokenVisitante(): Result<String> {
        visitorStorage.visitorTokenSync()?.takeIf { it.isNotBlank() }?.let {
            return Result.success(it)
        }

        return mutex.withLock {
            visitorStorage.visitorTokenSync()?.takeIf { it.isNotBlank() }?.let {
                return@withLock Result.success(it)
            }
            apiCaller.call { api.crearSesionVisitante() }
                .map { dto ->
                    visitorStorage.guardarIdentidadVisitante(dto.visitorId, dto.visitorToken)
                    dto.visitorToken
                }
        }
    }
}
