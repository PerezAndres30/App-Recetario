package com.perezgomez.recetario.features.auth.domain.repositories

import com.perezgomez.recetario.core.network.TokenProvider
import com.perezgomez.recetario.features.auth.domain.entities.Usuario

/** Abstracción para guardar la sesión; hoy se implementa con SharedPreferences. */
interface SessionStorage : TokenProvider {
    fun guardarSesion(token: String, usuario: Usuario)
    override fun token(): String?
    fun usuario(): Usuario?
    fun limpiar()
}
