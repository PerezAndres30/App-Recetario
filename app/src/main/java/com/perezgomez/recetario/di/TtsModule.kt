package com.perezgomez.recetario.di

import com.perezgomez.recetario.data.tts.AndroidTtsEngine
import com.perezgomez.recetario.domain.tts.RecetaTtsService
import com.perezgomez.recetario.domain.tts.RecetaTtsServiceImpl
import com.perezgomez.recetario.domain.tts.TtsEngine
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class TtsModule {

    @Binds
    @Singleton
    abstract fun bindTtsEngine(
        impl: AndroidTtsEngine,
    ): TtsEngine

    @Binds
    @Singleton
    abstract fun bindRecetaTtsService(
        impl: RecetaTtsServiceImpl,
    ): RecetaTtsService
}
