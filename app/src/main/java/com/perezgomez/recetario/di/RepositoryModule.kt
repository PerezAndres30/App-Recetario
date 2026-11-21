package com.perezgomez.recetario.di

import com.perezgomez.recetario.data.local.SharedPrefsSessionStorage
import com.perezgomez.recetario.data.repository.AuthRepositoryImpl
import com.perezgomez.recetario.data.repository.RecetaRepositoryImpl
import com.perezgomez.recetario.domain.repository.AuthRepository
import com.perezgomez.recetario.domain.repository.RecetaRepository
import com.perezgomez.recetario.domain.session.SessionStorage
import com.perezgomez.recetario.data.voice.AndroidVoiceRecognizer
import com.perezgomez.recetario.domain.voice.VoiceRecognizer
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

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

    @Binds
    @Singleton
    abstract fun bindRecetaRepository(
        impl: RecetaRepositoryImpl,
    ): RecetaRepository

    @Binds
    @Singleton
    abstract fun bindVoiceRecognizer(
        impl: AndroidVoiceRecognizer,
    ): VoiceRecognizer
}
