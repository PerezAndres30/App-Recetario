package com.perezgomez.recetario

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.perezgomez.recetario.core.navigation.AppNavHost
import com.perezgomez.recetario.core.navigation.Rutas
import com.perezgomez.recetario.features.auth.domain.repositories.AuthRepository
import com.perezgomez.recetario.ui.theme.RecetarioTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var authRepository: AuthRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Si hay sesión guardada se abre Home ("Mis recetas") directamente.
        // Si no hay sesión, se muestra la pantalla de bienvenida (Figma Frame 18).
        val inicio = if (authRepository.haySesion()) Rutas.HOME else Rutas.BIENVENIDA

        setContent {
            RecetarioTheme {
                AppNavHost(
                    startDestination = inicio,
                    authRepository = authRepository,
                )
            }
        }
    }
}
