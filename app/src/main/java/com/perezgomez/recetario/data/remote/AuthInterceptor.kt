package com.perezgomez.recetario.data.remote

import com.perezgomez.recetario.domain.session.SessionStorage
import javax.inject.Inject
import javax.inject.Singleton
import okhttp3.Interceptor
import okhttp3.Response

/** Agrega "Authorization: Bearer <token>" a cada petición cuando hay sesión. */
@Singleton
class AuthInterceptor @Inject constructor(private val session: SessionStorage) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val token = session.token()
        val request = if (token.isNullOrBlank()) {
            original
        } else {
            original.newBuilder().header("Authorization", "Bearer $token").build()
        }
        return chain.proceed(request)
    }
}
