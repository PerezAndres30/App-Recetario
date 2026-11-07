package com.perezgomez.recetario.data.datasource

import com.perezgomez.recetario.data.dto.LoginRequestDto
import com.perezgomez.recetario.data.dto.RegistroRequestDto
import com.perezgomez.recetario.data.dto.SesionDto
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {
    @POST("register")
    suspend fun registrar(@Body body: RegistroRequestDto): SesionDto

    @POST("login")
    suspend fun iniciarSesion(@Body body: LoginRequestDto): SesionDto
}
