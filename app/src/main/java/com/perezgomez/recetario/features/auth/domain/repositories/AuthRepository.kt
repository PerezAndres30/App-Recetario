package com.perezgomez.recetario.features.auth.domain.repositories

import com.perezgomez.recetario.features.auth.domain.entities.Usuario

interface AuthRepository {
    suspend fun registrar(nombre: String, correo: String, password: String): Result<Usuario>
    suspend fun iniciarSesion(correo: String, password: String): Result<Usuario>
    fun haySesion(): Boolean
    fun usuarioActual(): Usuario?
    fun cerrarSesion()
}
