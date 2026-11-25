package com.perezgomez.recetario.data.datasource

import com.perezgomez.recetario.data.dto.FirmaImagenDto
import com.perezgomez.recetario.data.dto.RecetaDto
import com.perezgomez.recetario.data.dto.RecetaRequestDto
import com.perezgomez.recetario.data.dto.RecetasResponseDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface RecetaApiService {
    /** [mias] = true pide solo las del usuario autenticado (requiere token). */
    @GET("recetas")
    suspend fun listar(@Query("mias") mias: Boolean? = null): RecetasResponseDto

    @GET("recetas/{id}")
    suspend fun obtener(@Path("id") id: String): RecetaDto

    @POST("recetas")
    suspend fun crear(@Body body: RecetaRequestDto): RecetaDto

    @PUT("recetas/{id}")
    suspend fun actualizar(@Path("id") id: String, @Body body: RecetaRequestDto): RecetaDto

    @POST("recetas/{id}/imagen-firma")
    suspend fun firmarImagen(@Path("id") id: String): FirmaImagenDto

    @DELETE("recetas/{id}")
    suspend fun eliminar(@Path("id") id: String)
}
