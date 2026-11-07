package com.perezgomez.recetario

import android.app.Application
import com.perezgomez.recetario.di.AppContainer

class RecetarioApp : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
