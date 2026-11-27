package com.perezgomez.recetario.features.auth.data.datasources.remote.api

import com.perezgomez.recetario.features.auth.data.datasources.remote.models.LoginRequestDto
import com.perezgomez.recetario.features.auth.data.datasources.remote.models.RegistroRequestDto
import com.perezgomez.recetario.features.auth.data.datasources.remote.models.SesionDto
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {
    @POST("register")
    suspend fun registrar(@Body body: RegistroRequestDto): SesionDto

    @POST("login")
    suspend fun iniciarSesion(@Body body: LoginRequestDto): SesionDto
}
