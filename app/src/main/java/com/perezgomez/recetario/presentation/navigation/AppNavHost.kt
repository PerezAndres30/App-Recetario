package com.perezgomez.recetario.presentation.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.perezgomez.recetario.di.AppContainer
import com.perezgomez.recetario.presentation.screens.LoginScreen
import com.perezgomez.recetario.presentation.screens.RegisterScreen
import com.perezgomez.recetario.presentation.viewmodels.LoginViewModel
import com.perezgomez.recetario.presentation.viewmodels.RegisterViewModel
import com.perezgomez.recetario.presentation.viewmodels.appViewModelFactory

@Composable
fun AppNavHost(
    container: AppContainer,
    startDestination: String,
    navController: NavHostController = rememberNavController(),
) {
    val factory = remember(container) { appViewModelFactory(container) }

    NavHost(navController = navController, startDestination = startDestination) {

        composable(Rutas.LOGIN) {
            LoginScreen(
                viewModel = viewModel<LoginViewModel>(factory = factory),
                onAutenticado = { navController.irAHomeLimpiandoPila() },
                onIrARegistro = { navController.navigate(Rutas.REGISTRO) },
            )
        }

        composable(Rutas.REGISTRO) {
            RegisterScreen(
                viewModel = viewModel<RegisterViewModel>(factory = factory),
                onRegistrado = { navController.irAHomeLimpiandoPila() },
                onVolver = { navController.popBackStack() },
            )
        }

        composable(Rutas.HOME) {
            // Provisional: la pantalla real de Home llega en el siguiente commit.
            HomeProvisional(
                onCerrarSesion = {
                    container.authRepository.cerrarSesion()
                    // Se vacía toda la pila para que "Atrás" no regrese a Home.
                    navController.navigate(Rutas.LOGIN) {
                        popUpTo(navController.graph.id) { inclusive = true }
                    }
                },
            )
        }
    }
}

@Composable
private fun HomeProvisional(onCerrarSesion: () -> Unit) {
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text("Sesión iniciada", style = MaterialTheme.typography.headlineMedium)
            Button(onClick = onCerrarSesion) { Text("Cerrar sesión") }
        }
    }
}

private fun NavHostController.irAHomeLimpiandoPila() {
    navigate(Rutas.HOME) {
        popUpTo(graph.id) { inclusive = true }
    }
}
