package com.perezgomez.recetario.data.local

import android.content.Context
import com.perezgomez.recetario.domain.model.Usuario
import com.perezgomez.recetario.domain.session.SessionStorage

class SharedPrefsSessionStorage(context: Context) : SessionStorage {

    private val prefs = context.applicationContext
        .getSharedPreferences(NOMBRE_ARCHIVO, Context.MODE_PRIVATE)

    override fun guardarSesion(token: String, usuario: Usuario) {
        prefs.edit()
            .putString(KEY_TOKEN, token)
            .putString(KEY_ID, usuario.id)
            .putString(KEY_NOMBRE, usuario.nombre)
            .putString(KEY_CORREO, usuario.correo)
            .apply()
    }

    override fun token(): String? = prefs.getString(KEY_TOKEN, null)

    override fun usuario(): Usuario? {
        val id = prefs.getString(KEY_ID, null) ?: return null
        return Usuario(
            id = id,
            nombre = prefs.getString(KEY_NOMBRE, "").orEmpty(),
            correo = prefs.getString(KEY_CORREO, "").orEmpty(),
        )
    }

    override fun limpiar() {
        prefs.edit().clear().apply()
    }

    private companion object {
        const val NOMBRE_ARCHIVO = "sesion"
        const val KEY_TOKEN = "token"
        const val KEY_ID = "usuario_id"
        const val KEY_NOMBRE = "usuario_nombre"
        const val KEY_CORREO = "usuario_correo"
    }
}
