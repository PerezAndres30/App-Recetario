package com.perezgomez.recetario.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.perezgomez.recetario.di.AppContainer
import com.perezgomez.recetario.presentation.screens.DetalleScreen
import com.perezgomez.recetario.presentation.screens.FormScreen
import com.perezgomez.recetario.presentation.screens.HomeScreen
import com.perezgomez.recetario.presentation.screens.LoginScreen
import com.perezgomez.recetario.presentation.screens.RegisterScreen
import com.perezgomez.recetario.presentation.viewmodels.DetalleViewModel
import com.perezgomez.recetario.presentation.viewmodels.FormViewModel
import com.perezgomez.recetario.presentation.viewmodels.HomeViewModel
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

        composable(Rutas.HOME) { entry ->
            val mensajeNav by entry.mensajeNavegacion()
            HomeScreen(
                viewModel = viewModel<HomeViewModel>(factory = factory),
                mensajeNav = mensajeNav,
                onMensajeNavMostrado = { entry.savedStateHandle.set<String>(Rutas.MENSAJE_NAV, null) },
                onNuevaReceta = { navController.navigate(Rutas.formulario()) },
                onVerDetalle = { id -> navController.navigate(Rutas.detalle(id)) },
                // Cerrar sesión: se vacía toda la pila para que "Atrás" no regrese a Home.
                onSesionCerrada = {
                    navController.navigate(Rutas.LOGIN) {
                        popUpTo(navController.graph.id) { inclusive = true }
                    }
                },
            )
        }

        composable(
            route = Rutas.DETALLE,
            arguments = listOf(navArgument(Rutas.ARG_ID) { type = NavType.StringType }),
        ) { entry ->
            val mensajeNav by entry.mensajeNavegacion()
            DetalleScreen(
                viewModel = viewModel<DetalleViewModel>(factory = factory),
                mensajeNav = mensajeNav,
                onMensajeNavMostrado = { entry.savedStateHandle.set<String>(Rutas.MENSAJE_NAV, null) },
                onVolver = { navController.popBackStack() },
                onEditar = { id -> navController.navigate(Rutas.formulario(id)) },
                onEliminada = { navController.volverConMensaje("Receta eliminada") },
            )
        }

        composable(
            route = Rutas.FORMULARIO,
            arguments = listOf(
                navArgument(Rutas.ARG_ID) {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
            ),
        ) {
            FormScreen(
                viewModel = viewModel<FormViewModel>(factory = factory),
                onVolver = { navController.popBackStack() },
                onGuardado = { mensaje -> navController.volverConMensaje(mensaje) },
            )
        }
    }
}

/** Mensaje pendiente que otra pantalla dejó para esta entrada de navegación. */
@Composable
private fun NavBackStackEntry.mensajeNavegacion() =
    savedStateHandle.getStateFlow<String?>(Rutas.MENSAJE_NAV, null).collectAsStateWithLifecycle()

private fun NavHostController.irAHomeLimpiandoPila() {
    navigate(Rutas.HOME) {
        popUpTo(graph.id) { inclusive = true }
    }
}

/** Regresa a la pantalla anterior dejándole un mensaje para mostrar en un Snackbar. */
private fun NavHostController.volverConMensaje(mensaje: String) {
    previousBackStackEntry?.savedStateHandle?.set(Rutas.MENSAJE_NAV, mensaje)
    popBackStack()
}
