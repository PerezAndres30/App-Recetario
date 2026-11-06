package com.perezgomez.recetario.domain.session

import com.perezgomez.recetario.domain.model.Usuario

/** Abstracción para guardar la sesión; hoy se implementa con SharedPreferences. */
interface SessionStorage {
    fun guardarSesion(token: String, usuario: Usuario)
    fun token(): String?
    fun usuario(): Usuario?
    fun limpiar()
}
