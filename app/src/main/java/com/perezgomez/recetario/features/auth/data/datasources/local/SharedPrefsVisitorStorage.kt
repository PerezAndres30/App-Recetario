package com.perezgomez.recetario.features.auth.data.datasources.local

import android.content.Context
import com.perezgomez.recetario.features.auth.domain.repositories.VisitorStorage
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SharedPrefsVisitorStorage @Inject constructor(
    @ApplicationContext context: Context,
) : VisitorStorage {

    private val prefs = context.applicationContext
        .getSharedPreferences(NOMBRE_ARCHIVO, Context.MODE_PRIVATE)

    override fun guardarIdentidadVisitante(visitorId: String, visitorToken: String) {
        prefs.edit()
            .putString(KEY_ACTIVE_ID, visitorId)
            .putString(KEY_ACTIVE_TOKEN, visitorToken)
            .apply()
    }

    override fun visitorTokenSync(): String? = prefs.getString(KEY_ACTIVE_TOKEN, null)

    override fun visitorIdSync(): String? = prefs.getString(KEY_ACTIVE_ID, null)

    override fun tokenParaLogin(correo: String): String? {
        val correoNorm = correo.trim().lowercase()
        val activo = prefs.getString(KEY_ACTIVE_TOKEN, null)
        if (!activo.isNullOrBlank()) return activo

        val pendienteCorreo = prefs.getString(KEY_PENDING_CORREO, null)
        val pendienteToken = prefs.getString(KEY_PENDING_TOKEN, null)
        return if (!pendienteToken.isNullOrBlank() && pendienteCorreo == correoNorm) {
            pendienteToken
        } else {
            null
        }
    }

    override fun tokenParaRegistro(): String? =
        prefs.getString(KEY_ACTIVE_TOKEN, null)?.takeIf { it.isNotBlank() }

    override fun registrarMigracionPendiente(userId: String, correo: String, token: String) {
        val editor = prefs.edit()
            .putString(KEY_PENDING_USER_ID, userId)
            .putString(KEY_PENDING_CORREO, correo.trim().lowercase())
            .putString(KEY_PENDING_TOKEN, token)

        if (prefs.getString(KEY_ACTIVE_TOKEN, null) == token) {
            editor.remove(KEY_ACTIVE_TOKEN).remove(KEY_ACTIVE_ID)
        }
        editor.apply()
    }

    override fun completarORetirarTokenUsado(
        tokenUsado: String,
        userId: String?,
        limpiarPendienteDeCuenta: Boolean,
    ) {
        val editor = prefs.edit()
        if (prefs.getString(KEY_ACTIVE_TOKEN, null) == tokenUsado) {
            editor.remove(KEY_ACTIVE_TOKEN).remove(KEY_ACTIVE_ID)
        }
        if (limpiarPendienteDeCuenta && prefs.getString(KEY_PENDING_TOKEN, null) == tokenUsado) {
            val pendienteUserId = prefs.getString(KEY_PENDING_USER_ID, null)
            if (userId == null || pendienteUserId == null || pendienteUserId == userId) {
                editor
                    .remove(KEY_PENDING_USER_ID)
                    .remove(KEY_PENDING_CORREO)
                    .remove(KEY_PENDING_TOKEN)
            }
        }
        editor.apply()
    }

    override fun rotarIdentidadVisitante() {
        prefs.edit()
            .remove(KEY_ACTIVE_TOKEN)
            .remove(KEY_ACTIVE_ID)
            .apply()
    }

    override fun hayMigracionPendiente(): Boolean =
        !prefs.getString(KEY_PENDING_TOKEN, null).isNullOrBlank()

    private companion object {
        const val NOMBRE_ARCHIVO = "identidad_visitante"
        const val KEY_ACTIVE_ID = "active_visitor_id"
        const val KEY_ACTIVE_TOKEN = "active_visitor_token"
        const val KEY_PENDING_USER_ID = "pending_user_id"
        const val KEY_PENDING_CORREO = "pending_correo"
        const val KEY_PENDING_TOKEN = "pending_visitor_token"
    }
}
