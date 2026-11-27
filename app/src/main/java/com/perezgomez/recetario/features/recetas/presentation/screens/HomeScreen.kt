package com.perezgomez.recetario.features.recetas.presentation.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.perezgomez.recetario.features.recetas.domain.entities.Receta
import com.perezgomez.recetario.core.ui.components.ImagenReceta
import com.perezgomez.recetario.core.ui.components.ConfirmarEliminarDialog
import com.perezgomez.recetario.core.ui.components.PantallaError
import com.perezgomez.recetario.features.recetas.presentation.viewmodels.HomeViewModel

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    modo: ModoHome,
    mensajeNav: String?,
    onMensajeNavMostrado: () -> Unit,
    onNuevaReceta: () -> Unit,
    onVerDetalle: (String) -> Unit,
    onIrAExplorar: () -> Unit = {},
    onIrAMisRecetas: () -> Unit = {},
    onIniciarSesion: () -> Unit = {},
    onSesionCerrada: () -> Unit = {},
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(modo) {
        viewModel.configurarModo(modo)
    }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { viewModel.cargar() }

    LaunchedEffect(state.mensaje) {
        state.mensaje?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onMensajeMostrado()
        }
    }

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
        onActualizar = viewModel::cargar,
        onIrAExplorar = onIrAExplorar,
        onIrAMisRecetas = onIrAMisRecetas,
        onIniciarSesion = onIniciarSesion,
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
    onActualizar: () -> Unit,
    onIrAExplorar: () -> Unit,
    onIrAMisRecetas: () -> Unit,
    onIniciarSesion: () -> Unit,
    onCerrarSesion: () -> Unit,
) {
    val esExplorar = state.modo == ModoHome.EXPLORAR

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
                title = {
                    Column {
                        Text(
                            text = if (esExplorar) "Explorar recetas" else "Mis recetas",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        if (esExplorar) {
                            Text(
                                text = "Catálogo público",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        } else if (state.nombreUsuario.isNotBlank()) {
                            Text(
                                text = "Hola, ${state.nombreUsuario}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = onActualizar,
                        enabled = !state.actualizando,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Actualizar recetas",
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }

                    if (esExplorar) {
                        if (state.estaAutenticado) {
                            // Usuario autenticado en Explorar -> Acceso directo a Mis recetas
                            TextButton(
                                onClick = onIrAMisRecetas,
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.textButtonColors(
                                    contentColor = MaterialTheme.colorScheme.primary,
                                ),
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.MenuBook,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Mis recetas",
                                    fontWeight = FontWeight.SemiBold,
                                )
                            }
                            IconButton(onClick = onCerrarSesion) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Logout,
                                    contentDescription = "Cerrar sesión",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        } else {
                            // Visitante en Explorar -> Iniciar sesión
                            TextButton(
                                onClick = onIniciarSesion,
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.textButtonColors(
                                    contentColor = MaterialTheme.colorScheme.primary,
                                ),
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Login,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Iniciar sesión",
                                    fontWeight = FontWeight.SemiBold,
                                )
                            }
                        }
                    } else {
                        // Usuario en Mis recetas -> Navegar a Explorar catálogo público
                        TextButton(
                            onClick = onIrAExplorar,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = MaterialTheme.colorScheme.primary,
                            ),
                        ) {
                            Icon(
                                imageVector = Icons.Default.Explore,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Explorar",
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                        IconButton(onClick = onCerrarSesion) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Logout,
                                contentDescription = "Cerrar sesión",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            // Solo se permite crear recetas desde la sección de Mis recetas
            if (!esExplorar && state.estaAutenticado) {
                FloatingActionButton(
                    onClick = onNuevaReceta,
                    shape = RoundedCornerShape(20.dp),
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Nueva receta")
                }
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
        ) {
            // Indicador de actualización con estilo salvia
            if (state.actualizando && state.lista !is ListaUi.Cargando) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = if (esExplorar) "Actualizando catálogo…" else "Actualizando tus recetas…",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            ) {
                when (val lista = state.lista) {
                    ListaUi.Cargando -> Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (esExplorar) "Cargando catálogo público…" else "Cargando tus recetas…",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    ListaUi.Vacia -> Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(96.dp),
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Restaurant,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(44.dp),
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                            text = if (esExplorar) "No hay recetas disponibles" else "Aún no tienes recetas propias",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onBackground,
                            textAlign = TextAlign.Center,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (esExplorar) {
                                "Vuelve a consultar más tarde para descubrir nuevas recetas."
                            } else {
                                "Las recetas del catálogo pertenecen a la comunidad. Crea tu primera receta con el botón + para gestionarla aquí."
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                        )
                        if (!esExplorar && state.estaAutenticado) {
                            Spacer(modifier = Modifier.height(20.dp))
                            Button(
                                onClick = onNuevaReceta,
                                shape = RoundedCornerShape(24.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary,
                                ),
                                modifier = Modifier.height(48.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Nueva receta",
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                                )
                            }
                        }
                    }
                    is ListaUi.Error -> PantallaError(
                        mensaje = lista.mensaje,
                        onReintentar = onReintentar,
                    )
                    is ListaUi.Contenido -> LazyColumn(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        items(lista.recetas, key = { it.id }) { receta ->
                            RecetaItem(
                                receta = receta,
                                puedeEliminar = receta.esPropia && state.estaAutenticado,
                                onClick = { onVerDetalle(receta.id) },
                                onEliminar = { onEliminarClick(receta) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RecetaItem(
    receta: Receta,
    puedeEliminar: Boolean,
    onClick: () -> Unit,
    onEliminar: () -> Unit,
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Miniatura: la foto de la receta, o un ícono si no tiene
            ImagenReceta(
                origen = receta.imagenUrl,
                descripcion = "Foto de ${receta.titulo}",
                modifier = Modifier.size(56.dp),
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = receta.titulo,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    // Chip de categoría
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                    ) {
                        Text(
                            text = receta.categoria,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        )
                    }

                    // Chip de tiempo con icono de reloj
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(13.dp),
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = "${receta.tiempoMinutos} min",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    // Porciones
                    Text(
                        text = "· ${receta.porciones} porc.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            // Indicador visual de favorita
            if (receta.favorita) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    modifier = Modifier.size(32.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = "Favorita",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(17.dp),
                        )
                    }
                }
                Spacer(modifier = Modifier.width(4.dp))
            }

            // Acción de eliminar solo disponible para recetas verificadas en Mis recetas
            if (puedeEliminar) {
                IconButton(
                    onClick = onEliminar,
                    modifier = Modifier.size(36.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Eliminar receta",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
        }
    }
}
