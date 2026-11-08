package com.perezgomez.recetario.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.perezgomez.recetario.domain.model.Receta
import com.perezgomez.recetario.presentation.components.ConfirmarEliminarDialog
import com.perezgomez.recetario.presentation.components.PantallaCargando
import com.perezgomez.recetario.presentation.components.PantallaError
import com.perezgomez.recetario.presentation.viewmodels.HomeViewModel

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    mensajeNav: String?,
    onMensajeNavMostrado: () -> Unit,
    onNuevaReceta: () -> Unit,
    onVerDetalle: (String) -> Unit,
    onSesionCerrada: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { viewModel.cargar() }

    LaunchedEffect(state.mensaje) {
        state.mensaje?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onMensajeMostrado()
        }
    }

    // Mensaje que dejó otra pantalla al volver ("Receta creada", "Receta eliminada"...).
    LaunchedEffect(mensajeNav) {
        mensajeNav?.let {
            snackbarHostState.showSnackbar(it)
            onMensajeNavMostrado()
        }
    }

    LaunchedEffect(state.sesionCerrada) {
        if (state.sesionCerrada) onSesionCerrada()
    }

    HomeContent(
        state = state,
        snackbarHostState = snackbarHostState,
        onNuevaReceta = onNuevaReceta,
        onVerDetalle = onVerDetalle,
        onEliminarClick = viewModel::onEliminarClick,
        onReintentar = viewModel::cargar,
        onCerrarSesion = viewModel::onCerrarSesion,
    )

    state.recetaAEliminar?.let { receta ->
        ConfirmarEliminarDialog(
            titulo = receta.titulo,
            cargando = state.eliminando,
            onConfirmar = viewModel::onConfirmarEliminar,
            onCancelar = viewModel::onCancelarEliminar,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeContent(
    state: HomeUiState,
    snackbarHostState: SnackbarHostState,
    onNuevaReceta: () -> Unit,
    onVerDetalle: (String) -> Unit,
    onEliminarClick: (Receta) -> Unit,
    onReintentar: () -> Unit,
    onCerrarSesion: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Mis recetas")
                        if (state.nombreUsuario.isNotBlank()) {
                            Text(
                                text = "Hola, ${state.nombreUsuario}",
                                style = MaterialTheme.typography.labelMedium,
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = onCerrarSesion) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Cerrar sesión")
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(onClick = onNuevaReceta) {
                Icon(Icons.Default.Add, contentDescription = "Nueva receta")
            }
        },
    ) { padding ->
        Box(modifier = Modifier
            .padding(padding)
            .fillMaxSize()) {
            when (val lista = state.lista) {
                ListaUi.Cargando -> PantallaCargando()
                ListaUi.Vacia -> Text(
                    text = "Aún no tienes recetas.\nToca + para crear la primera.",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(24.dp),
                )
                is ListaUi.Error -> PantallaError(mensaje = lista.mensaje, onReintentar = onReintentar)
                is ListaUi.Contenido -> LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(lista.recetas, key = { it.id }) { receta ->
                        RecetaItem(
                            receta = receta,
                            onClick = { onVerDetalle(receta.id) },
                            onEliminar = { onEliminarClick(receta) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RecetaItem(receta: Receta, onClick: () -> Unit, onEliminar: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, top = 8.dp, bottom = 8.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(receta.titulo, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = "${receta.categoria} · ${receta.tiempoMinutos} min · ${receta.porciones} porc.",
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            if (receta.favorita) {
                Icon(
                    Icons.Default.Favorite,
                    contentDescription = "Favorita",
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
            IconButton(onClick = onEliminar) {
                Icon(Icons.Default.Delete, contentDescription = "Eliminar receta")
            }
        }
    }
}
