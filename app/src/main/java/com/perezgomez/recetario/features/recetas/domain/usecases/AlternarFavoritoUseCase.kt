package com.perezgomez.recetario.features.recetas.domain.usecases

import com.perezgomez.recetario.core.domain.error.AppException
import com.perezgomez.recetario.features.auth.domain.repositories.AuthRepository
import com.perezgomez.recetario.features.recetas.domain.repositories.RecetaRepository
import javax.inject.Inject

/**
 * Valida la regla de negocio de Favoritos (colección privada exclusiva para cuentas autenticadas)
 * antes de guardar o retirar una receta (propia o ajena) en el servidor.
 */
class AlternarFavoritoUseCase @Inject constructor(
    private val recetaRepository: RecetaRepository,
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(recetaId: String, guardar: Boolean): Result<Boolean> {
        if (!authRepository.haySesion()) {
            return Result.failure(AppException("Inicia sesión para guardar recetas en favoritos."))
        }
        return if (guardar) {
            recetaRepository.guardarFavorito(recetaId)
        } else {
            recetaRepository.quitarFavorito(recetaId)
        }
    }
}
