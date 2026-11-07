package com.perezgomez.recetario.presentation.viewmodels

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.perezgomez.recetario.di.AppContainer

/** Única fábrica de ViewModels: cada uno recibe sus dependencias del [AppContainer] por constructor. */
fun appViewModelFactory(container: AppContainer): ViewModelProvider.Factory = viewModelFactory {
    initializer { LoginViewModel(container.authRepository) }
    initializer { RegisterViewModel(container.authRepository) }
}
