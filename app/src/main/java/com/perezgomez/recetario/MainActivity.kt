package com.perezgomez.recetario

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.perezgomez.recetario.presentation.navigation.AppNavHost
import com.perezgomez.recetario.presentation.navigation.Rutas
import com.perezgomez.recetario.presentation.theme.RecetarioTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val container = (application as RecetarioApp).container
        // Con sesión guardada se salta el login y se abre Home directamente.
        val inicio = if (container.authRepository.haySesion()) Rutas.HOME else Rutas.LOGIN

        setContent {
            RecetarioTheme {
                AppNavHost(container = container, startDestination = inicio)
            }
        }
    }
}
