package com.perezgomez.recetario.features.recetas.di

import com.perezgomez.recetario.features.recetas.data.datasources.remote.api.RecetaApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import retrofit2.Retrofit

@Module
@InstallIn(SingletonComponent::class)
object RecetasNetworkModule {

    @Provides
    @Singleton
    fun provideRecetaApiService(retrofit: Retrofit): RecetaApiService {
        return retrofit.create(RecetaApiService::class.java)
    }
}
