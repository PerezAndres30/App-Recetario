package com.perezgomez.recetario.domain.repository

import com.perezgomez.recetario.domain.model.Usuario

interface AuthRepository {
    suspend fun registrar(nombre: String, correo: String, password: String): Result<Usuario>
    suspend fun iniciarSesion(correo: String, password: String): Result<Usuario>
    fun haySesion(): Boolean
    fun usuarioActual(): Usuario?
    fun cerrarSesion()
}
