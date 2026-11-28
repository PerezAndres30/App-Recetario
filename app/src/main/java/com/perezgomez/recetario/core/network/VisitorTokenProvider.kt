package com.perezgomez.recetario.core.network

/** Proveedor síncrono del token anónimo firmado para que AuthInterceptor lo adjunte como visitante. */
interface VisitorTokenProvider {
    fun visitorTokenSync(): String?
}
