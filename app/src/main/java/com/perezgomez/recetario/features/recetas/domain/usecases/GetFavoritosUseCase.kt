package com.perezgomez.recetario.features.recetas.domain.usecases

import com.perezgomez.recetario.features.auth.domain.repositories.AuthRepository
import com.perezgomez.recetario.features.recetas.domain.entities.Receta
import com.perezgomez.recetario.features.recetas.domain.repositories.RecetaRepository
import javax.inject.Inject

/**
 * Obtiene la colección privada de favoritos del usuario autenticado desde GET /favoritos.
 * Si no existe sesión activa devuelve lista vacía sin realizar llamadas protegidas.
 */
class GetFavoritosUseCase @Inject constructor(
    private val recetaRepository: RecetaRepository,
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(): Result<List<Receta>> {
        if (!authRepository.haySesion()) {
            return Result.success(emptyList())
        }
        return recetaRepository.listarFavoritos()
    }
}
