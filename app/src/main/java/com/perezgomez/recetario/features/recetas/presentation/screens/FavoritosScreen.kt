package com.perezgomez.recetario.features.recetas.presentation.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.perezgomez.recetario.core.ui.components.BarraNavegacionEditorial
import com.perezgomez.recetario.core.ui.components.DestinoBarraUsuario
import com.perezgomez.recetario.core.ui.components.EncabezadoEditorial
import com.perezgomez.recetario.core.ui.components.PantallaCargando
import com.perezgomez.recetario.core.ui.components.PantallaError
import com.perezgomez.recetario.core.ui.components.SelloEditorial
import com.perezgomez.recetario.core.ui.components.TarjetaRecetaCatalogo
import com.perezgomez.recetario.features.recetas.domain.entities.Ingrediente
import com.perezgomez.recetario.features.recetas.domain.entities.Receta
import com.perezgomez.recetario.features.recetas.presentation.viewmodels.FavoritosViewModel
import com.perezgomez.recetario.ui.theme.AmarilloSuave
import com.perezgomez.recetario.ui.theme.BordeEditorial
import com.perezgomez.recetario.ui.theme.CremaPapel
import com.perezgomez.recetario.ui.theme.CremaVainilla
import com.perezgomez.recetario.ui.theme.RecetarioTheme
import com.perezgomez.recetario.ui.theme.RojoCereza
import com.perezgomez.recetario.ui.theme.TextoPrincipal

@Immutable
data class FavoritoTarjetaUi(
    val receta: Receta,
    val likesCount: Int = receta.likesCount,
    val leDiLike: Boolean = receta.leDiLike,
)

/**
 * Vista privada de Favoritos exclusiva para usuarios registrados conectada al servidor.
 */
@Composable
fun FavoritosScreen(
    viewModel: FavoritosViewModel,
    onIrAExplorar: () -> Unit,
    onIrAMisRecetas: () -> Unit,
    onVerDetalle: (String) -> Unit,
    onSesionCerrada: () -> Unit,
    onSesionExpirada: () -> Unit = onSesionCerrada,
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

    LaunchedEffect(state.sesionCerrada) {
        if (state.sesionCerrada) onSesionCerrada()
    }

    LaunchedEffect(state.sesionExpirada) {
        if (state.sesionExpirada) onSesionExpirada()
    }

    FavoritosContent(
        nombreUsuario = state.nombreUsuario,
        lista = state.lista,
        actualizando = state.actualizando,
        snackbarHostState = snackbarHostState,
        onActualizar = viewModel::cargar,
        onReintentar = viewModel::cargar,
        onIrAExplorar = onIrAExplorar,
        onIrAMisRecetas = onIrAMisRecetas,
        onVerDetalle = onVerDetalle,
        onToggleLike = viewModel::onToggleLike,
        onQuitarFavorito = viewModel::onQuitarFavorito,
        onCerrarSesion = viewModel::onCerrarSesion,
    )
}

@Composable
fun FavoritosContent(
    nombreUsuario: String,
    lista: FavoritosListaUi,
    actualizando: Boolean = false,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onActualizar: () -> Unit = {},
    onReintentar: () -> Unit = {},
    onIrAExplorar: () -> Unit,
    onIrAMisRecetas: () -> Unit,
    onVerDetalle: (String) -> Unit,
    onToggleLike: (String) -> Unit,
    onQuitarFavorito: (String) -> Unit,
    onCerrarSesion: () -> Unit,
) {
    val densidad = LocalDensity.current
    val minCelda = if (densidad.fontScale > 1.3f) 260.dp else 160.dp

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            EncabezadoEditorial(
                titulo = "Favoritos",
                subtitulo = if (nombreUsuario.isNotBlank()) "Colección guardada de $nombreUsuario" else "Recetas guardadas",
                acciones = {
                    if (actualizando) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .padding(horizontal = 8.dp)
                                .size(20.dp),
                            strokeWidth = 2.dp,
                            color = RojoCereza,
                        )
                    } else {
                        IconButton(
                            onClick = onActualizar,
                            modifier = Modifier.defaultMinSize(minWidth = 48.dp, minHeight = 48.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Actualizar",
                                tint = TextoPrincipal,
                            )
                        }
                    }
                    IconButton(
                        onClick = onCerrarSesion,
                        modifier = Modifier.size(48.dp),
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = "Cerrar sesión",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
            )
        },
        bottomBar = {
            BarraNavegacionEditorial(
                destinoActual = DestinoBarraUsuario.FAVORITOS,
                onIrAExplorar = onIrAExplorar,
                onIrAMisRecetas = onIrAMisRecetas,
                onIrAFavoritos = {},
            )
        },
    ) { padding ->
        when (lista) {
            FavoritosListaUi.Cargando -> PantallaCargando(Modifier.padding(padding))
            is FavoritosListaUi.Error -> PantallaError(
                mensaje = lista.mensaje,
                onReintentar = onReintentar,
                modifier = Modifier.padding(padding),
            )
            FavoritosListaUi.Vacia -> {
                Box(
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = CremaPapel,
                            border = BorderStroke(1.dp, BordeEditorial),
                            modifier = Modifier.size(72.dp),
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Bookmark,
                                    contentDescription = null,
                                    tint = RojoCereza,
                                    modifier = Modifier.size(32.dp),
                                )
                            }
                        }
                        SelloEditorial(
                            texto = "COLECCIÓN PRIVADA",
                            fondo = AmarilloSuave,
                            textoColor = TextoPrincipal,
                        )
                        Text(
                            text = "Aún no tienes recetas en Favoritos",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center,
                        )
                        Text(
                            text = "Explora el catálogo y toca el marcador en cualquier receta que quieras conservar a la mano.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Button(
                            onClick = onIrAExplorar,
                            shape = RoundedCornerShape(50),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = RojoCereza,
                                contentColor = CremaVainilla,
                            ),
                            modifier = Modifier.defaultMinSize(minHeight = 48.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Default.Explore,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Descubrir recetas")
                        }
                    }
                }
            }
            is FavoritosListaUi.Contenido -> {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = minCelda),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp),
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize(),
                ) {
                    items(lista.recetas, key = { it.id }) { receta ->
                        TarjetaRecetaCatalogo(
                            receta = receta,
                            onClick = { onVerDetalle(receta.id) },
                            likesCount = receta.likesCount,
                            leDiLike = receta.leDiLike,
                            onLikeClick = { onToggleLike(receta.id) },
                            mostrarFavorito = true,
                            esFavorito = receta.esFavorito,
                            onFavoritoClick = { onQuitarFavorito(receta.id) },
                            puedeEliminar = false,
                            onEliminarClick = {},
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Favoritos con recetas")
@Composable
private fun FavoritosConRecetasPreview() {
    val muestraInicial = listOf(
        Receta(
            id = "fav-1",
            titulo = "Tarta de Fresa y Vainilla",
            categoria = "postre",
            tiempoMinutos = 45,
            porciones = 6,
            ingredientes = listOf(Ingrediente("Fresas", "300", "g")),
            pasos = listOf("Hornear"),
            esPropia = true,
            likesCount = 24,
            leDiLike = true,
            esFavorito = true,
        ),
        Receta(
            id = "fav-2",
            titulo = "Pasta al Limón con Albahaca",
            categoria = "comida",
            tiempoMinutos = 20,
            porciones = 2,
            ingredientes = listOf(Ingrediente("Espagueti", "200", "g")),
            pasos = listOf("Cocer pasta"),
            esPropia = false,
            likesCount = 41,
            leDiLike = false,
            esFavorito = true,
        ),
    )
    var lista by remember { mutableStateOf(muestraInicial) }
    RecetarioTheme {
        FavoritosContent(
            nombreUsuario = "Ana Sofía",
            lista = FavoritosListaUi.Contenido(lista),
            onIrAExplorar = {},
            onIrAMisRecetas = {},
            onVerDetalle = {},
            onToggleLike = { id ->
                lista = lista.map { item ->
                    if (item.id == id) {
                        val nuevo = !item.leDiLike
                        item.copy(
                            leDiLike = nuevo,
                            likesCount = if (nuevo) item.likesCount + 1 else (item.likesCount - 1).coerceAtLeast(0),
                        )
                    } else {
                        item
                    }
                }
            },
            onQuitarFavorito = { id ->
                lista = lista.filterNot { it.id == id }
            },
            onCerrarSesion = {},
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Favoritos estado vacío")
@Composable
private fun FavoritosVacioPreview() {
    RecetarioTheme {
        FavoritosContent(
            nombreUsuario = "Ana Sofía",
            lista = FavoritosListaUi.Vacia,
            onIrAExplorar = {},
            onIrAMisRecetas = {},
            onVerDetalle = {},
            onToggleLike = {},
            onQuitarFavorito = {},
            onCerrarSesion = {},
        )
    }
}
