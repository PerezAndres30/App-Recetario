package com.perezgomez.recetario.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.perezgomez.recetario.features.auth.domain.repositories.AuthRepository
import com.perezgomez.recetario.features.auth.presentation.screens.BienvenidaScreen
import com.perezgomez.recetario.features.auth.presentation.screens.LoginScreen
import com.perezgomez.recetario.features.auth.presentation.screens.RegisterScreen
import com.perezgomez.recetario.features.auth.presentation.viewmodels.LoginViewModel
import com.perezgomez.recetario.features.auth.presentation.viewmodels.RegisterViewModel
import com.perezgomez.recetario.features.recetas.presentation.screens.DetalleScreen
import com.perezgomez.recetario.features.recetas.presentation.screens.FormScreen
import com.perezgomez.recetario.features.recetas.presentation.screens.HomeScreen
import com.perezgomez.recetario.features.recetas.presentation.screens.ModoHome
import com.perezgomez.recetario.features.recetas.presentation.viewmodels.DetalleViewModel
import com.perezgomez.recetario.features.recetas.presentation.viewmodels.FormViewModel
import com.perezgomez.recetario.features.recetas.presentation.viewmodels.HomeViewModel

@Composable
fun AppNavHost(
    startDestination: String,
    authRepository: AuthRepository,
    navController: NavHostController = rememberNavController(),
) {
    NavHost(navController = navController, startDestination = startDestination) {

        composable(Rutas.BIENVENIDA) {
            BienvenidaScreen(
                onExplorarVisitante = { navController.navigate(Rutas.EXPLORAR) },
                onIniciarSesion = { navController.navigate(Rutas.LOGIN) },
                onCrearCuenta = { navController.navigate(Rutas.REGISTRO) },
            )
        }

        composable(Rutas.LOGIN) {
            LoginScreen(
                viewModel = hiltViewModel<LoginViewModel>(),
                onAutenticado = { navController.irAHomeLimpiandoPila() },
                onIrARegistro = { navController.navigate(Rutas.REGISTRO) },
                onContinuarComoVisitante = { navController.navigate(Rutas.EXPLORAR) },
            )
        }

        composable(Rutas.REGISTRO) {
            RegisterScreen(
                viewModel = hiltViewModel<RegisterViewModel>(),
                onRegistrado = { navController.irAHomeLimpiandoPila() },
                onVolver = { navController.popBackStack() },
                onContinuarComoVisitante = { navController.navigate(Rutas.EXPLORAR) },
            )
        }

        // Catálogo público: accesible tanto para visitantes como para usuarios autenticados
        composable(Rutas.EXPLORAR) { entry ->
            val mensajeNav by entry.mensajeNavegacion()
            HomeScreen(
                viewModel = hiltViewModel<HomeViewModel>(),
                modo = ModoHome.EXPLORAR,
                mensajeNav = mensajeNav,
                onMensajeNavMostrado = { entry.savedStateHandle.set<String>(Rutas.MENSAJE_NAV, null) },
                onNuevaReceta = {
                    if (authRepository.haySesion()) {
                        navController.navigate(Rutas.formulario())
                    } else {
                        navController.navigate(Rutas.LOGIN)
                    }
                },
                onVerDetalle = { id -> navController.navigate(Rutas.detalle(id)) },
                onIrAMisRecetas = { navController.navigate(Rutas.HOME) },
                onIniciarSesion = { navController.navigate(Rutas.LOGIN) },
                onSesionCerrada = { navController.irABienvenidaLimpiandoPila() },
            )
        }

        // Mis recetas: exclusivo para usuarios autenticados
        composable(Rutas.HOME) { entry ->
            if (!authRepository.haySesion()) {
                LaunchedEffect(Unit) {
                    navController.irABienvenidaLimpiandoPila()
                }
            } else {
                val mensajeNav by entry.mensajeNavegacion()
                HomeScreen(
                    viewModel = hiltViewModel<HomeViewModel>(),
                    modo = ModoHome.MIS_RECETAS,
                    mensajeNav = mensajeNav,
                    onMensajeNavMostrado = { entry.savedStateHandle.set<String>(Rutas.MENSAJE_NAV, null) },
                    onNuevaReceta = { navController.navigate(Rutas.formulario()) },
                    onVerDetalle = { id -> navController.navigate(Rutas.detalle(id)) },
                    onIrAExplorar = { navController.navigate(Rutas.EXPLORAR) },
                    onIniciarSesion = { navController.navigate(Rutas.LOGIN) },
                    onSesionCerrada = { navController.irABienvenidaLimpiandoPila() },
                )
            }
        }

        composable(
            route = Rutas.DETALLE,
            arguments = listOf(navArgument(Rutas.ARG_ID) { type = NavType.StringType }),
        ) { entry ->
            val mensajeNav by entry.mensajeNavegacion()
            DetalleScreen(
                viewModel = hiltViewModel<DetalleViewModel>(),
                mensajeNav = mensajeNav,
                onMensajeNavMostrado = { entry.savedStateHandle.set<String>(Rutas.MENSAJE_NAV, null) },
                onVolver = { navController.popBackStack() },
                onEditar = { id ->
                    if (authRepository.haySesion()) {
                        navController.navigate(Rutas.formulario(id))
                    } else {
                        navController.navigate(Rutas.LOGIN)
                    }
                },
                onEliminada = { navController.volverConMensaje("Receta eliminada") },
            )
        }

        // Ruta protegida: crear y editar recetas
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
            if (!authRepository.haySesion()) {
                LaunchedEffect(Unit) {
                    navController.navigate(Rutas.LOGIN) {
                        popUpTo(Rutas.FORMULARIO) { inclusive = true }
                    }
                }
            } else {
                FormScreen(
                    viewModel = hiltViewModel<FormViewModel>(),
                    onVolver = { navController.popBackStack() },
                    onGuardado = { mensaje -> navController.volverConMensaje(mensaje) },
                )
            }
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

private fun NavHostController.irABienvenidaLimpiandoPila() {
    navigate(Rutas.BIENVENIDA) {
        popUpTo(graph.id) { inclusive = true }
    }
}

/** Regresa a la pantalla anterior dejándole un mensaje para mostrar en un Snackbar. */
private fun NavHostController.volverConMensaje(mensaje: String) {
    previousBackStackEntry?.savedStateHandle?.set(Rutas.MENSAJE_NAV, mensaje)
    popBackStack()
}
