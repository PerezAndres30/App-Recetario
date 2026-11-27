package com.perezgomez.recetario.features.auth.data.repositories

import com.perezgomez.recetario.core.network.ApiCaller
import com.perezgomez.recetario.features.auth.data.datasources.remote.api.AuthApiService
import com.perezgomez.recetario.features.auth.data.datasources.remote.mapper.toDomain
import com.perezgomez.recetario.features.auth.data.datasources.remote.models.LoginRequestDto
import com.perezgomez.recetario.features.auth.data.datasources.remote.models.RegistroRequestDto
import com.perezgomez.recetario.features.auth.data.datasources.remote.models.SesionDto
import com.perezgomez.recetario.features.auth.domain.entities.Usuario
import com.perezgomez.recetario.features.auth.domain.repositories.AuthRepository
import com.perezgomez.recetario.features.auth.domain.repositories.SessionStorage
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
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
