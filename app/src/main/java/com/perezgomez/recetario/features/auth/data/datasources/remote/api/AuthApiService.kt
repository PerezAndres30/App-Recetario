package com.perezgomez.recetario.features.auth.data.datasources.remote.api

import com.perezgomez.recetario.features.auth.data.datasources.remote.models.LoginRequestDto
import com.perezgomez.recetario.features.auth.data.datasources.remote.models.RegistroRequestDto
import com.perezgomez.recetario.features.auth.data.datasources.remote.models.SesionDto
import com.perezgomez.recetario.features.auth.data.datasources.remote.models.VisitanteSesionDto
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface AuthApiService {
    @POST("visitante/sesion")
    suspend fun crearSesionVisitante(): VisitanteSesionDto

    @POST("register")
    suspend fun registrar(
        @Body body: RegistroRequestDto,
        @Header("X-Visitor-Token") visitorToken: String? = null,
    ): SesionDto

    @POST("login")
    suspend fun iniciarSesion(
        @Body body: LoginRequestDto,
        @Header("X-Visitor-Token") visitorToken: String? = null,
    ): SesionDto
}
