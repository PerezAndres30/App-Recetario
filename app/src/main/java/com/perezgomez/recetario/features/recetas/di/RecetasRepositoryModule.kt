package com.perezgomez.recetario.features.recetas.di

import com.perezgomez.recetario.features.recetas.data.repositories.RecetaRepositoryImpl
import com.perezgomez.recetario.features.recetas.domain.repositories.RecetaRepository
import com.perezgomez.recetario.features.recetas.domain.tts.RecetaTtsService
import com.perezgomez.recetario.features.recetas.domain.tts.RecetaTtsServiceImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RecetasRepositoryModule {

    @Binds
    @Singleton
    abstract fun bindRecetaRepository(
        impl: RecetaRepositoryImpl,
    ): RecetaRepository

    @Binds
    @Singleton
    abstract fun bindRecetaTtsService(
        impl: RecetaTtsServiceImpl,
    ): RecetaTtsService
}
