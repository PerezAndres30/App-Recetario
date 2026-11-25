package com.perezgomez.recetario.di

import com.perezgomez.recetario.data.imagen.AndroidProcesadorImagen
import com.perezgomez.recetario.data.local.SharedPrefsSessionStorage
import com.perezgomez.recetario.data.repository.AuthRepositoryImpl
import com.perezgomez.recetario.data.repository.ImagenRepositoryImpl
import com.perezgomez.recetario.data.repository.RecetaRepositoryImpl
import com.perezgomez.recetario.domain.imagen.ProcesadorImagen
import com.perezgomez.recetario.domain.repository.ImagenRepository
import com.perezgomez.recetario.domain.repository.AuthRepository
import com.perezgomez.recetario.domain.repository.RecetaRepository
import com.perezgomez.recetario.domain.session.SessionStorage
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
    abstract fun bindImagenRepository(
        impl: ImagenRepositoryImpl,
    ): ImagenRepository

    @Binds
    @Singleton
    abstract fun bindProcesadorImagen(
        impl: AndroidProcesadorImagen,
    ): ProcesadorImagen

    @Binds
    @Singleton
    abstract fun bindRecetaRepository(
        impl: RecetaRepositoryImpl,
    ): RecetaRepository
}
