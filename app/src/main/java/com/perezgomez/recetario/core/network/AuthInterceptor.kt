package com.perezgomez.recetario.core.network

import javax.inject.Inject
import javax.inject.Singleton
import okhttp3.Interceptor
import okhttp3.Response

/** Agrega "Authorization: Bearer <token>" a cada petición cuando hay sesión. */
@Singleton
class AuthInterceptor @Inject constructor(private val tokenProvider: TokenProvider) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val token = tokenProvider.token()
        val request = if (token.isNullOrBlank()) {
            original
        } else {
            original.newBuilder().header("Authorization", "Bearer $token").build()
        }
        return chain.proceed(request)
    }
}
