package com.perezgomez.recetario.di

import android.content.Context
import com.perezgomez.recetario.BuildConfig
import com.perezgomez.recetario.data.datasource.AuthApiService
import com.perezgomez.recetario.data.datasource.RecetaApiService
import com.perezgomez.recetario.data.local.SharedPrefsSessionStorage
import com.perezgomez.recetario.data.remote.ApiCaller
import com.perezgomez.recetario.data.remote.AuthInterceptor
import com.perezgomez.recetario.data.repository.AuthRepositoryImpl
import com.perezgomez.recetario.data.repository.RecetaRepositoryImpl
import com.perezgomez.recetario.domain.repository.AuthRepository
import com.perezgomez.recetario.domain.repository.RecetaRepository
import com.perezgomez.recetario.domain.session.SessionStorage
import java.util.concurrent.TimeUnit
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

/**
 * Contenedor de dependencias manual. Se crea una sola vez en [com.perezgomez.recetario.RecetarioApp]
 * y es el único lugar que construye Retrofit, OkHttp, el almacenamiento y los repositorios.
 */
class AppContainer(context: Context) {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    val sessionStorage: SessionStorage = SharedPrefsSessionStorage(context)

    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(AuthInterceptor(sessionStorage))
        .addInterceptor(
            HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC
                redactHeader("Authorization")
            },
        )
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.API_BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(json.asConverterFactory("application/json; charset=UTF-8".toMediaType()))
        .build()

    private val apiCaller = ApiCaller(json)

    val authRepository: AuthRepository = AuthRepositoryImpl(
        api = retrofit.create(AuthApiService::class.java),
        session = sessionStorage,
        apiCaller = apiCaller,
    )

    val recetaRepository: RecetaRepository = RecetaRepositoryImpl(
        api = retrofit.create(RecetaApiService::class.java),
        apiCaller = apiCaller,
    )
}
