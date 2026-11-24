package com.perezgomez.recetario.util

import com.perezgomez.recetario.domain.model.Receta
import com.perezgomez.recetario.domain.model.Usuario
import com.perezgomez.recetario.domain.repository.AuthRepository
import com.perezgomez.recetario.domain.repository.RecetaRepository
import com.perezgomez.recetario.domain.session.SessionStorage
class FakeRecetaRepository : RecetaRepository {
    val recetas = mutableListOf<Receta>()
    val recetasPropias = mutableSetOf<String>()
    var deberiaFallar: Boolean = false
    var mensajeError: String = "Error simulado en repositorio"

    var llamadasListar: Int = 0
    var llamadasCrear: Int = 0
    var llamadasActualizar: Int = 0
    var llamadasEliminar: Int = 0

    override fun esPropietario(recetaId: String): Boolean = recetasPropias.contains(recetaId)

    override fun registrarRecetaPropia(recetaId: String) {
        if (recetaId.isNotBlank()) recetasPropias.add(recetaId)
    }

    override suspend fun listar(): Result<List<Receta>> {
        llamadasListar++
        return if (deberiaFallar) Result.failure(Exception(mensajeError))
        else Result.success(recetas.toList())
    }

    override suspend fun obtener(id: String): Result<Receta> {
        val receta = recetas.find { it.id == id }
        return if (deberiaFallar || receta == null) {
            Result.failure(Exception(mensajeError))
        } else {
            Result.success(receta)
        }
    }

    override suspend fun crear(receta: Receta): Result<Receta> {
        llamadasCrear++
        return if (deberiaFallar) {
            Result.failure(Exception(mensajeError))
        } else {
            val creada = receta.copy(id = if (receta.id.isBlank()) "id-${recetas.size + 1}" else receta.id)
            recetas.add(creada)
            registrarRecetaPropia(creada.id)
            Result.success(creada)
        }
    }

    override suspend fun actualizar(receta: Receta): Result<Receta> {
        llamadasActualizar++
        return if (deberiaFallar) {
            Result.failure(Exception(mensajeError))
        } else {
            val index = recetas.indexOfFirst { it.id == receta.id }
            if (index != -1) recetas[index] = receta
            Result.success(receta)
        }
    }

    override suspend fun eliminar(id: String): Result<Unit> {
        llamadasEliminar++
        return if (deberiaFallar) {
            Result.failure(Exception(mensajeError))
        } else {
            recetas.removeAll { it.id == id }
            recetasPropias.remove(id)
            Result.success(Unit)
        }
    }
}

class FakeAuthRepository(
    private var usuario: Usuario? = Usuario("user-1", "Chef Ivan", "ivan@test.com"),
) : AuthRepository {
    override fun haySesion(): Boolean = usuario != null
    override fun usuarioActual(): Usuario? = usuario
    override suspend fun iniciarSesion(correo: String, password: String): Result<Usuario> =
        Result.success(usuario ?: Usuario("user-1", "Chef Ivan", correo))
    override suspend fun registrar(nombre: String, correo: String, password: String): Result<Usuario> =
        Result.success(Usuario("user-1", nombre, correo))
    override fun cerrarSesion() { usuario = null }
}

class FakeSessionStorage(
    private var tokenActual: String? = "token-test-123",
    private var usuarioActual: Usuario? = Usuario("user-1", "Chef Ivan", "ivan@test.com"),
) : SessionStorage {
    override fun guardarSesion(token: String, usuario: Usuario) {
        tokenActual = token
        usuarioActual = usuario
    }
    override fun token(): String? = tokenActual
    override fun usuario(): Usuario? = usuarioActual
    override fun limpiar() {
        tokenActual = null
        usuarioActual = null
    }
}
