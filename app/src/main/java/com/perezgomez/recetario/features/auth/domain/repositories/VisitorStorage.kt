package com.perezgomez.recetario.features.auth.domain.repositories

import com.perezgomez.recetario.core.network.VisitorTokenProvider

/**
 * Almacenamiento persistente y privado de la identidad anónima del visitante,
 * separado del almacenamiento de sesión autenticada para sobrevivir cierres de sesión
 * y aislar migraciones incompletas por cuenta destinataria.
 */
interface VisitorStorage : VisitorTokenProvider {
    fun guardarIdentidadVisitante(visitorId: String, visitorToken: String)
    override fun visitorTokenSync(): String?
    fun visitorIdSync(): String?

    /** Token elegible para enviar en login (activo del visitante o pendiente de esa misma cuenta). */
    fun tokenParaLogin(correo: String): String?

    /** Token elegible para registro (solo token activo de visitante; nunca pendientes de otra cuenta). */
    fun tokenParaRegistro(): String?

    /** Conserva un token cuya migración quedó parcial, vinculado a la cuenta destinataria. */
    fun registrarMigracionPendiente(userId: String, correo: String, token: String)

    /** Retira el token ya migrado por completo o bloqueado para esta cuenta. */
    fun completarORetirarTokenUsado(tokenUsado: String, userId: String? = null, limpiarPendienteDeCuenta: Boolean = true)

    /** Rota el token activo actual del visitante al cerrar sesión o cambiar de cuenta. */
    fun rotarIdentidadVisitante()

    fun hayMigracionPendiente(): Boolean
}
