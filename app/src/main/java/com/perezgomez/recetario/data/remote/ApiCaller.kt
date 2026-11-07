package com.perezgomez.recetario.data.remote

import com.perezgomez.recetario.data.dto.ErrorDto
import com.perezgomez.recetario.domain.error.AppException
import java.io.IOException
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import retrofit2.HttpException

/** Ejecuta llamadas a la API y convierte cualquier fallo en un [AppException] con mensaje legible. */
class ApiCaller(private val json: Json) {

    suspend fun <T> call(block: suspend () -> T): Result<T> = try {
        Result.success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: HttpException) {
        Result.failure(AppException(mensajeHttp(e)))
    } catch (e: IOException) {
        Result.failure(AppException("No hay conexión con el servidor. Revisa tu internet e inténtalo de nuevo."))
    } catch (e: SerializationException) {
        Result.failure(AppException("El servidor respondió algo inesperado."))
    }

    private fun mensajeHttp(e: HttpException): String {
        val cuerpo = runCatching { e.response()?.errorBody()?.string() }.getOrNull()
        val mensajeApi = cuerpo?.let { runCatching { json.decodeFromString<ErrorDto>(it).error }.getOrNull() }
        return mensajeApi ?: when (e.code()) {
            401 -> "Tu sesión expiró. Inicia sesión de nuevo."
            404 -> "No se encontró lo que buscabas."
            in 500..599 -> "Error del servidor. Inténtalo más tarde."
            else -> "Ocurrió un error inesperado (${e.code()})."
        }
    }
}
