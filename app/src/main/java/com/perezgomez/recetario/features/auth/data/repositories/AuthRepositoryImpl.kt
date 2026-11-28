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
import com.perezgomez.recetario.features.auth.domain.repositories.VisitorStorage
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val api: AuthApiService,
    private val session: SessionStorage,
    private val visitorStorage: VisitorStorage,
    private val apiCaller: ApiCaller,
) : AuthRepository {

    override suspend fun registrar(nombre: String, correo: String, password: String): Result<Usuario> {
        val tokenVisitante = visitorStorage.tokenParaRegistro()
        return apiCaller
            .call { api.registrar(RegistroRequestDto(nombre, correo, password), tokenVisitante) }
            .map { dto -> guardarSesionYActualizarMigracion(dto, tokenVisitante) }
    }

    override suspend fun iniciarSesion(correo: String, password: String): Result<Usuario> {
        val tokenVisitante = visitorStorage.tokenParaLogin(correo)
        return apiCaller
            .call { api.iniciarSesion(LoginRequestDto(correo, password), tokenVisitante) }
            .map { dto -> guardarSesionYActualizarMigracion(dto, tokenVisitante) }
    }

    override fun haySesion(): Boolean = !session.token().isNullOrBlank()

    override fun usuarioActual(): Usuario? = session.usuario()

    override fun cerrarSesion() {
        session.limpiar()
        // Rota la identidad activa sin borrar tokens pendientes de recuperación vinculados a la cuenta.
        visitorStorage.rotarIdentidadVisitante()
    }

    private fun guardarSesionYActualizarMigracion(dto: SesionDto, tokenVisitanteUsado: String?): Usuario {
        val usuario = dto.usuario.toDomain()
        session.guardarSesion(dto.token, usuario)

        if (!tokenVisitanteUsado.isNullOrBlank()) {
            val estado = dto.migracionLikes
            when {
                estado == null || estado.completada -> {
                    visitorStorage.completarORetirarTokenUsado(tokenVisitanteUsado, usuario.id)
                }
                estado.bloqueadaPorOtraCuenta -> {
                    // Otro usuario ya reclamó ese token: retirar solo como token activo de esta sesión.
                    visitorStorage.completarORetirarTokenUsado(
                        tokenUsado = tokenVisitanteUsado,
                        userId = usuario.id,
                        limpiarPendienteDeCuenta = false,
                    )
                }
                estado.parcial -> {
                    // Migración incompleta: conservar el token ligado únicamente a esta cuenta para recuperarlo después.
                    visitorStorage.registrarMigracionPendiente(usuario.id, usuario.correo, tokenVisitanteUsado)
                }
            }
        }
        return usuario
    }
}
