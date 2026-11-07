package com.perezgomez.recetario

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.perezgomez.recetario.presentation.screens.LoginScreen
import com.perezgomez.recetario.presentation.screens.RegisterScreen
import com.perezgomez.recetario.presentation.theme.RecetarioTheme
import com.perezgomez.recetario.presentation.viewmodels.appViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val container = (application as RecetarioApp).container

        setContent {
            RecetarioTheme {
                // Cambio de pantalla provisional: se reemplaza por Navigation Compose en el siguiente paso.
                val factory = remember { appViewModelFactory(container) }
                var pantalla by rememberSaveable { mutableStateOf("login") }
                when (pantalla) {
                    "login" -> LoginScreen(
                        viewModel = viewModel(factory = factory),
                        onAutenticado = { pantalla = "sesion" },
                        onIrARegistro = { pantalla = "registro" },
                    )
                    "registro" -> RegisterScreen(
                        viewModel = viewModel(factory = factory),
                        onRegistrado = { pantalla = "sesion" },
                        onVolver = { pantalla = "login" },
                    )
                    else -> Surface(modifier = Modifier.fillMaxSize()) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("Sesión iniciada", style = MaterialTheme.typography.headlineMedium)
                        }
                    }
                }
            }
        }
    }
}
