package com.perezgomez.recetario.features.recetas.domain.usecases

import com.perezgomez.recetario.features.auth.domain.repositories.AuthRepository
import com.perezgomez.recetario.features.recetas.domain.entities.Receta
import com.perezgomez.recetario.features.recetas.domain.repositories.RecetaRepository
import javax.inject.Inject

/**
 * Obtiene las recetas del usuario autenticado solicitando ?mias=true a la API
 * y verificando la marca de propiedad [Receta.esPropia] devuelta por el servidor.
 */
class GetMisRecetasUseCase @Inject constructor(
    private val recetaRepository: RecetaRepository,
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(): Result<List<Receta>> {
        if (!authRepository.haySesion()) {
            return Result.success(emptyList())
        }
        return recetaRepository.listar(soloMias = true).map { recetas ->
            recetas.filter { it.esPropia }
        }
    }
}
