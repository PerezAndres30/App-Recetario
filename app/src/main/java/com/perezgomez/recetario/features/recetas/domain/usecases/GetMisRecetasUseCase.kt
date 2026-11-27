package com.perezgomez.recetario.features.recetas.domain.usecases

import com.perezgomez.recetario.features.auth.domain.repositories.AuthRepository
import com.perezgomez.recetario.features.recetas.domain.entities.Receta
import com.perezgomez.recetario.features.recetas.domain.repositories.RecetaRepository
import javax.inject.Inject

/**
 * Obtiene las recetas cuya autoría provisional está verificada para el usuario autenticado.
 * Nota: la autoría local es provisional mientras el backend no devuelva propietario y
 * no sustituye la autorización definitiva del servidor.
 */
class GetMisRecetasUseCase @Inject constructor(
    private val recetaRepository: RecetaRepository,
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(): Result<List<Receta>> {
        if (!authRepository.haySesion()) {
            return Result.success(emptyList())
        }
        return recetaRepository.listar().map { recetas ->
            recetas.filter { recetaRepository.esPropietario(it.id) }
        }
    }
}
