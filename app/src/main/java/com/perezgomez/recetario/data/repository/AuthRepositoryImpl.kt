package com.perezgomez.recetario.data.repository

import com.perezgomez.recetario.data.datasource.AuthApiService
import com.perezgomez.recetario.data.dto.LoginRequestDto
import com.perezgomez.recetario.data.dto.RegistroRequestDto
import com.perezgomez.recetario.data.dto.SesionDto
import com.perezgomez.recetario.data.mapper.toDomain
import com.perezgomez.recetario.data.remote.ApiCaller
import com.perezgomez.recetario.domain.model.Usuario
import com.perezgomez.recetario.domain.repository.AuthRepository
import com.perezgomez.recetario.domain.session.SessionStorage

class AuthRepositoryImpl(
    private val api: AuthApiService,
    private val session: SessionStorage,
    private val apiCaller: ApiCaller,
) : AuthRepository {

    override suspend fun registrar(nombre: String, correo: String, password: String): Result<Usuario> =
        apiCaller
            .call { api.registrar(RegistroRequestDto(nombre, correo, password)) }
            .map(::guardarSesion)

    override suspend fun iniciarSesion(correo: String, password: String): Result<Usuario> =
        apiCaller
            .call { api.iniciarSesion(LoginRequestDto(correo, password)) }
            .map(::guardarSesion)

    override fun haySesion(): Boolean = !session.token().isNullOrBlank()

    override fun usuarioActual(): Usuario? = session.usuario()

    override fun cerrarSesion() = session.limpiar()

    private fun guardarSesion(dto: SesionDto): Usuario {
        val usuario = dto.usuario.toDomain()
        session.guardarSesion(dto.token, usuario)
        return usuario
    }
}
