package com.perezgomez.recetario.features.auth.di

import com.perezgomez.recetario.core.network.TokenProvider
import com.perezgomez.recetario.features.auth.data.datasources.local.SharedPrefsSessionStorage
import com.perezgomez.recetario.features.auth.data.repositories.AuthRepositoryImpl
import com.perezgomez.recetario.features.auth.domain.repositories.AuthRepository
import com.perezgomez.recetario.features.auth.domain.repositories.SessionStorage
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AuthRepositoryModule {

    @Binds
    @Singleton
    abstract fun bindTokenProvider(
        impl: SharedPrefsSessionStorage,
    ): TokenProvider

    @Binds
    @Singleton
    abstract fun bindSessionStorage(
        impl: SharedPrefsSessionStorage,
    ): SessionStorage

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        impl: AuthRepositoryImpl,
    ): AuthRepository
}
