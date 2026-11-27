package com.perezgomez.recetario.core.network

/**
 * Proveedor mínimo del token de autenticación para desacoplar [AuthInterceptor]
 * de los módulos concretos de sesión y features.
 */
fun interface TokenProvider {
    fun token(): String?
}
