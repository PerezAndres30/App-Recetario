package com.perezgomez.recetario.presentation.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.perezgomez.recetario.domain.model.Receta
import com.perezgomez.recetario.presentation.components.PantallaCargando
import com.perezgomez.recetario.presentation.components.PantallaError
import com.perezgomez.recetario.presentation.viewmodels.DetalleViewModel

@Composable
fun DetalleScreen(
    viewModel: DetalleViewModel,
    onVolver: () -> Unit,
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

    DetalleContent(
        state = state,
        snackbarHostState = snackbarHostState,
        onVolver = onVolver,
        onReintentar = viewModel::cargar,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleContent(
    state: DetalleUiState,
    snackbarHostState: SnackbarHostState,
    onVolver: () -> Unit,
    onReintentar: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Receta") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        when (val contenido = state.contenido) {
            DetalleContenido.Cargando -> PantallaCargando(Modifier.padding(padding))
            is DetalleContenido.Error -> PantallaError(
                mensaje = contenido.mensaje,
                onReintentar = onReintentar,
                modifier = Modifier.padding(padding),
            )
            is DetalleContenido.Listo -> RecetaCompleta(contenido.receta, Modifier.padding(padding))
        }
    }
}

@Composable
private fun RecetaCompleta(receta: Receta, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
    ) {
        Text(receta.titulo, style = MaterialTheme.typography.headlineSmall)
        if (receta.descripcion.isNotBlank()) {
            Text(receta.descripcion, modifier = Modifier.padding(top = 4.dp))
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

        Dato("Categoría", receta.categoria)
        Dato("Tiempo de preparación", "${receta.tiempoMinutos} min")
        Dato("Porciones", receta.porciones.toString())
        Dato("Favorita", if (receta.favorita) "Sí" else "No")
        if (receta.creadaEn.length >= 10) Dato("Creada el", receta.creadaEn.take(10))

        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

        Text("Ingredientes", style = MaterialTheme.typography.titleMedium)
        receta.ingredientes.forEach { ingrediente ->
            val cantidad = listOf(ingrediente.cantidad, ingrediente.unidad)
                .filter { it.isNotBlank() }
                .joinToString(" ")
            Text(
                text = "• " + if (cantidad.isBlank()) ingrediente.nombre else "$cantidad de ${ingrediente.nombre}",
                modifier = Modifier.padding(top = 4.dp),
            )
        }

        Text(
            "Pasos",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(top = 16.dp),
        )
        receta.pasos.forEachIndexed { indice, paso ->
            Text(text = "${indice + 1}. $paso", modifier = Modifier.padding(top = 4.dp))
        }
    }
}

@Composable
private fun Dato(etiqueta: String, valor: String) {
    Column(modifier = Modifier.padding(bottom = 8.dp)) {
        Text(etiqueta, style = MaterialTheme.typography.labelMedium)
        Text(valor, style = MaterialTheme.typography.bodyLarge)
    }
}
