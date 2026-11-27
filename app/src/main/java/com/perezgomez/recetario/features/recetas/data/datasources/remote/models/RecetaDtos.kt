package com.perezgomez.recetario.features.recetas.data.datasources.remote.models

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class IngredienteDto(
    val nombre: String,
    val cantidad: String = "",
    val unidad: String = "",
)

/** Lo que devuelve la API. */
@Serializable
data class RecetaDto(
    val recetaId: String,
    val titulo: String,
    val descripcion: String = "",
    val categoria: String = "otro",
    val tiempoMinutos: Int = 0,
    val porciones: Int = 1,
    val ingredientes: List<IngredienteDto> = emptyList(),
    val pasos: List<String> = emptyList(),
    val favorita: Boolean = false,
    val creadaEn: String = "",
    val actualizadaEn: String = "",
    val esPropia: Boolean = false,
    val imagenUrl: String? = null,
    val imagenPublicId: String? = null,
)

/**
 * Lo que se envía a la API en POST y PUT.
 *
 * [imagenUrl] e [imagenPublicId] tienen tres estados, y la API los distingue:
 * null (campo omitido) = conservar la foto; JsonNull = quitarla; texto = establecerla.
 * Por eso son JsonElement y NO se serializan cuando valen null de Kotlin.
 */
@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class RecetaRequestDto(
    val titulo: String,
    val descripcion: String,
    val categoria: String,
    val tiempoMinutos: Int,
    val porciones: Int,
    val ingredientes: List<IngredienteDto>,
    val pasos: List<String>,
    val favorita: Boolean,
    @EncodeDefault(EncodeDefault.Mode.NEVER) val imagenUrl: JsonElement? = null,
    @EncodeDefault(EncodeDefault.Mode.NEVER) val imagenPublicId: JsonElement? = null,
)

/** Respuesta de POST /recetas/{id}/imagen-firma: todo lo necesario para subir a Cloudinary. */
@Serializable
data class FirmaImagenDto(
    val uploadUrl: String,
    val cloudName: String,
    val apiKey: String,
    val timestamp: Long,
    val signature: String,
    val publicId: String,
    val allowedFormats: String,
    val overwrite: Boolean,
    val invalidate: Boolean,
)

/** Respuesta de Cloudinary tras subir la foto (solo lo que usamos). */
@Serializable
data class CloudinaryRespuestaDto(
    @kotlinx.serialization.SerialName("secure_url") val secureUrl: String,
    @kotlinx.serialization.SerialName("public_id") val publicId: String,
)

@Serializable
data class RecetasResponseDto(
    val recetas: List<RecetaDto>,
)
