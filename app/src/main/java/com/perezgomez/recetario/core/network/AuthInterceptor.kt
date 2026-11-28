package com.perezgomez.recetario.core.network

import javax.inject.Inject
import javax.inject.Singleton
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Prioriza "Authorization: Bearer <token>" cuando hay sesión activa.
 * Solo si no hay JWT activo, adjunta "X-Visitor-Token" si la petición no lo especificó explícitamente.
 */
@Singleton
class AuthInterceptor @Inject constructor(
    private val tokenProvider: TokenProvider,
    private val visitorTokenProvider: VisitorTokenProvider,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val token = tokenProvider.token()
        val builder = original.newBuilder()

        if (!token.isNullOrBlank()) {
            builder.header("Authorization", "Bearer $token")
        } else if (original.header("X-Visitor-Token") == null) {
            val visitorToken = visitorTokenProvider.visitorTokenSync()
            if (!visitorToken.isNullOrBlank()) {
                builder.header("X-Visitor-Token", visitorToken)
            }
        }

        return chain.proceed(builder.build())
    }
}
