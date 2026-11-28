package com.perezgomez.recetario.core.network

import com.perezgomez.recetario.core.domain.error.AppException
import com.perezgomez.recetario.core.domain.error.UnauthorizedException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import retrofit2.HttpException

/** Ejecuta llamadas a la API y convierte cualquier fallo en un [AppException] con mensaje legible. */
@Singleton
class ApiCaller @Inject constructor(private val json: Json) {

    suspend fun <T> call(block: suspend () -> T): Result<T> = try {
        Result.success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: HttpException) {
        Result.failure(crearErrorHttp(e))
    } catch (e: IOException) {
        Result.failure(AppException("No hay conexión con el servidor. Revisa tu internet e inténtalo de nuevo."))
    } catch (e: SerializationException) {
        Result.failure(AppException("El servidor respondió algo inesperado."))
    }

    private fun crearErrorHttp(e: HttpException): AppException {
        val cuerpo = runCatching { e.response()?.errorBody()?.string() }.getOrNull()
        val mensajeApi = cuerpo?.let { runCatching { json.decodeFromString<ErrorDto>(it).error }.getOrNull() }
        if (e.code() == 401) {
            return UnauthorizedException(mensajeApi ?: "Tu sesión expiró. Inicia sesión de nuevo.")
        }
        val mensaje = mensajeApi ?: when (e.code()) {
            404 -> "No se encontró lo que buscabas."
            409 -> "Conflicto temporal al procesar la solicitud. Intenta de nuevo."
            in 500..599 -> "Error del servidor. Inténtalo más tarde."
            else -> "Ocurrió un error inesperado (${e.code()})."
        }
        return AppException(mensaje)
    }
}
