package com.perezgomez.recetario.features.recetas.domain.usecases

import com.perezgomez.recetario.core.domain.error.AppException
import com.perezgomez.recetario.features.auth.domain.repositories.AuthRepository
import com.perezgomez.recetario.features.recetas.domain.repositories.RecetaRepository
import javax.inject.Inject

/**
 * Verifica que exista una sesión activa y que la receta pertenezca al usuario autenticado
 * antes de delegar la eliminación al repositorio.
 * Nota: la autoría local es provisional y no sustituye la autorización definitiva del backend.
 */
class EliminarRecetaPropiaUseCase @Inject constructor(
    private val recetaRepository: RecetaRepository,
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(recetaId: String): Result<Unit> {
        if (!authRepository.haySesion()) {
            return Result.failure(AppException("Debes iniciar sesión para eliminar recetas."))
        }
        if (!recetaRepository.esPropietario(recetaId)) {
            return Result.failure(AppException("Solo puedes eliminar recetas creadas por ti."))
        }
        return recetaRepository.eliminar(recetaId)
    }
}
