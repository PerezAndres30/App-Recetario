package com.perezgomez.recetario

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.perezgomez.recetario.domain.repository.AuthRepository
import com.perezgomez.recetario.presentation.navigation.AppNavHost
import com.perezgomez.recetario.presentation.navigation.Rutas
import com.perezgomez.recetario.presentation.theme.RecetarioTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var authRepository: AuthRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Con sesión guardada se salta el login y se abre Home directamente.
        val inicio = if (authRepository.haySesion()) Rutas.HOME else Rutas.LOGIN

        setContent {
            RecetarioTheme {
                AppNavHost(startDestination = inicio)
            }
        }
    }
}
