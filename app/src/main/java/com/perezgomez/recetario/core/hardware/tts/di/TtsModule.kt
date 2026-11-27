package com.perezgomez.recetario.core.hardware.tts.di

import com.perezgomez.recetario.core.hardware.tts.data.AndroidTtsEngine
import com.perezgomez.recetario.core.hardware.tts.domain.TtsEngine
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
}
