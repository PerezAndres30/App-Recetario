package com.perezgomez.recetario.data.datasource

import com.perezgomez.recetario.data.dto.RecetaDto
import com.perezgomez.recetario.data.dto.RecetaRequestDto
import com.perezgomez.recetario.data.dto.RecetasResponseDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface RecetaApiService {
    @GET("recetas")
    suspend fun listar(): RecetasResponseDto

    @GET("recetas/{id}")
    suspend fun obtener(@Path("id") id: String): RecetaDto

    @POST("recetas")
    suspend fun crear(@Body body: RecetaRequestDto): RecetaDto

    @PUT("recetas/{id}")
    suspend fun actualizar(@Path("id") id: String, @Body body: RecetaRequestDto): RecetaDto

    @DELETE("recetas/{id}")
    suspend fun eliminar(@Path("id") id: String)
}
