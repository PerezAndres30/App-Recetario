package com.perezgomez.recetario.data.repository

import com.perezgomez.recetario.data.datasource.RecetaApiService
import com.perezgomez.recetario.data.dto.CloudinaryRespuestaDto
import com.perezgomez.recetario.data.dto.FirmaImagenDto
import com.perezgomez.recetario.data.remote.ApiCaller
import com.perezgomez.recetario.domain.error.AppException
import com.perezgomez.recetario.domain.model.ImagenSubida
import com.perezgomez.recetario.domain.repository.ImagenRepository
import java.io.File
import java.io.IOException
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody

/**
 * Sube la foto en dos pasos: (1) la API firma la subida, (2) la app envía el archivo a Cloudinary.
 * El paso 2 usa un OkHttpClient aparte, SIN el token de la API, para no filtrarlo a un tercero.
 */
@Singleton
class ImagenRepositoryImpl @Inject constructor(
    private val api: RecetaApiService,
    private val apiCaller: ApiCaller,
    @Named("cloudinary") private val clienteCloudinary: OkHttpClient,
    private val json: Json,
) : ImagenRepository {

    override suspend fun subir(recetaId: String, rutaArchivo: String): Result<ImagenSubida> {
        val firma = apiCaller.call { api.firmarImagen(recetaId) }.getOrElse { return Result.failure(it) }
        return subirACloudinary(firma, File(rutaArchivo))
    }

    private suspend fun subirACloudinary(firma: FirmaImagenDto, archivo: File): Result<ImagenSubida> =
        withContext(Dispatchers.IO) {
            try {
                // Los valores deben ser exactamente los firmados; cualquier cambio da "Invalid Signature".
                val cuerpo = MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("api_key", firma.apiKey)
                    .addFormDataPart("timestamp", firma.timestamp.toString())
                    .addFormDataPart("signature", firma.signature)
                    .addFormDataPart("public_id", firma.publicId)
                    .addFormDataPart("allowed_formats", firma.allowedFormats)
                    .addFormDataPart("overwrite", firma.overwrite.toString())
                    .addFormDataPart("invalidate", firma.invalidate.toString())
                    .addFormDataPart("file", "foto.jpg", archivo.asRequestBody("image/jpeg".toMediaType()))
                    .build()
                val peticion = Request.Builder().url(firma.uploadUrl).post(cuerpo).build()

                clienteCloudinary.newCall(peticion).execute().use { respuesta ->
                    val texto = respuesta.body?.string().orEmpty()
                    if (!respuesta.isSuccessful) {
                        Result.failure(AppException("No se pudo subir la foto (${respuesta.code}). Inténtalo de nuevo."))
                    } else {
                        val dto = json.decodeFromString<CloudinaryRespuestaDto>(texto)
                        Result.success(ImagenSubida(url = dto.secureUrl, publicId = dto.publicId))
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: IOException) {
                Result.failure(AppException("No hay conexión para subir la foto. Revisa tu internet."))
            } catch (e: Exception) {
                Result.failure(AppException("Cloudinary respondió algo inesperado al subir la foto."))
            }
        }
}
