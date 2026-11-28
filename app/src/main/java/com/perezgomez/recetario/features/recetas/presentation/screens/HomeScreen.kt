package com.perezgomez.recetario.features.recetas.presentation.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.perezgomez.recetario.core.ui.components.BarraNavegacionEditorial
import com.perezgomez.recetario.core.ui.components.ConfirmarEliminarDialog
import com.perezgomez.recetario.core.ui.components.DestinoBarraUsuario
import com.perezgomez.recetario.core.ui.components.EncabezadoEditorial
import com.perezgomez.recetario.core.ui.components.HeroCulinarioEditorial
import com.perezgomez.recetario.core.ui.components.PantallaCargando
import com.perezgomez.recetario.core.ui.components.PantallaError
import com.perezgomez.recetario.core.ui.components.SeparadorOndulado
import com.perezgomez.recetario.core.ui.components.TarjetaRecetaCatalogo
import com.perezgomez.recetario.features.recetas.domain.entities.Ingrediente
import com.perezgomez.recetario.features.recetas.domain.entities.Receta
import com.perezgomez.recetario.features.recetas.presentation.viewmodels.HomeViewModel
import com.perezgomez.recetario.ui.theme.BordeEditorial
import com.perezgomez.recetario.ui.theme.CremaCalida
import com.perezgomez.recetario.ui.theme.CremaPapel
import com.perezgomez.recetario.ui.theme.CremaVainilla
import com.perezgomez.recetario.ui.theme.RecetarioTheme
import com.perezgomez.recetario.ui.theme.RojoCereza
import com.perezgomez.recetario.ui.theme.RosaFresa
import com.perezgomez.recetario.ui.theme.TextoPrincipal
import com.perezgomez.recetario.ui.theme.TextoSecundario
import kotlinx.coroutines.launch

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
    onIrAFavoritos: () -> Unit = {},
    onIniciarSesion: () -> Unit = {},
    onSesionCerrada: () -> Unit = {},
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

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

    LaunchedEffect(state.sesionExpirada) {
        if (state.sesionExpirada && state.modo == ModoHome.MIS_RECETAS) {
            onIniciarSesion()
        }
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
        onIrAFavoritos = onIrAFavoritos,
        onIniciarSesion = onIniciarSesion,
        onCerrarSesion = viewModel::onCerrarSesion,
        onLikeClick = viewModel::onToggleLike,
        onFavoritoClick = viewModel::onToggleFavorito,
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

@Composable
fun HomeContent(
    state: HomeUiState,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onNuevaReceta: () -> Unit,
    onVerDetalle: (String) -> Unit,
    onEliminarClick: (Receta) -> Unit,
    onReintentar: () -> Unit,
    onActualizar: () -> Unit,
    onIrAExplorar: () -> Unit,
    onIrAMisRecetas: () -> Unit,
    onIrAFavoritos: () -> Unit = {},
    onIniciarSesion: () -> Unit,
    onCerrarSesion: () -> Unit,
    likesPorReceta: Map<String, Pair<Int, Boolean>> = emptyMap(),
    favoritosPorReceta: Set<String> = emptySet(),
    onLikeClick: (String) -> Unit = {},
    onFavoritoClick: (String) -> Unit = {},
) {
    val esExplorar = state.modo == ModoHome.EXPLORAR
    val densidad = LocalDensity.current
    val minCelda = if (densidad.fontScale > 1.25f) 260.dp else 160.dp

    Scaffold(
        containerColor = CremaVainilla,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            val tituloHeader = if (esExplorar) "Explorar recetas" else "Mis recetas"
            val subtituloHeader = when {
                state.estaAutenticado && !esExplorar && state.nombreUsuario.isNotBlank() ->
                    "Hola, ${state.nombreUsuario}"
                state.estaAutenticado && esExplorar && state.nombreUsuario.isNotBlank() ->
                    "Cocina de ${state.nombreUsuario}"
                esExplorar -> "Edición de cocina abierta"
                else -> "Colección personal"
            }

            EncabezadoEditorial(
                titulo = tituloHeader,
                subtitulo = subtituloHeader,
                acciones = {
                    if (state.actualizando) {
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
                                Icons.Default.Refresh,
                                contentDescription = "Actualizar",
                                tint = TextoPrincipal,
                            )
                        }
                    }

                    if (!state.estaAutenticado) {
                        Surface(
                            onClick = onIniciarSesion,
                            shape = RoundedCornerShape(50),
                            color = RojoCereza,
                            modifier = Modifier.defaultMinSize(minHeight = 48.dp),
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp),
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Filled.Login,
                                    contentDescription = null,
                                    tint = CremaVainilla,
                                    modifier = Modifier.size(16.dp),
                                )
                                Text(
                                    text = "Iniciar sesión",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = CremaVainilla,
                                )
                            }
                        }
                    } else {
                        IconButton(
                            onClick = onCerrarSesion,
                            modifier = Modifier.defaultMinSize(minWidth = 48.dp, minHeight = 48.dp),
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.Logout,
                                contentDescription = "Cerrar sesión",
                                tint = TextoPrincipal,
                            )
                        }
                    }
                },
            )
        },
        bottomBar = {
            if (state.estaAutenticado) {
                BarraNavegacionEditorial(
                    destinoActual = if (esExplorar) {
                        DestinoBarraUsuario.EXPLORAR
                    } else {
                        DestinoBarraUsuario.MIS_RECETAS
                    },
                    onIrAExplorar = onIrAExplorar,
                    onIrAMisRecetas = onIrAMisRecetas,
                    onIrAFavoritos = onIrAFavoritos,
                )
            }
        },
        floatingActionButton = {
            if (state.estaAutenticado) {
                ExtendedFloatingActionButton(
                    onClick = onNuevaReceta,
                    shape = RoundedCornerShape(50),
                    containerColor = RojoCereza,
                    contentColor = CremaVainilla,
                    icon = {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Nueva receta",
                        )
                    },
                    text = {
                        Text(
                            text = "Nueva receta",
                            style = MaterialTheme.typography.labelLarge,
                        )
                    },
                )
            }
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            when (val lista = state.lista) {
                ListaUi.Cargando -> PantallaCargando()
                is ListaUi.Error -> PantallaError(lista.mensaje, onReintentar = onReintentar)
                ListaUi.Vacia -> EstadoVacioEditorial(
                    esExplorar = esExplorar,
                    estaAutenticado = state.estaAutenticado,
                    onNuevaReceta = onNuevaReceta,
                    onIniciarSesion = onIniciarSesion,
                )

                is ListaUi.Contenido -> LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = minCelda),
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        top = 12.dp,
                        bottom = 96.dp,
                    ),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    if (esExplorar) {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                HeroCulinarioEditorial(
                                    etiquetaSuperior = "RECETARIO COMUNITARIO",
                                    titulo = "¿Qué cocinamos hoy?",
                                    subtitulo = if (state.estaAutenticado) {
                                        "Explora creaciones reales de la comunidad o escucha cualquier platillo paso a paso."
                                    } else {
                                        "Explora el catálogo público y escucha cada preparación con lectura por voz."
                                    },
                                    modifier = Modifier.clip(RoundedCornerShape(20.dp)),
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text(
                                        text = "CATÁLOGO EDITORIAL",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            letterSpacing = 1.2.sp,
                                            fontWeight = FontWeight.SemiBold,
                                        ),
                                        color = RojoCereza,
                                    )
                                    Text(
                                        text = "${lista.recetas.size} platillos",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = TextoSecundario,
                                    )
                                }
                            }
                        }
                    } else {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text(
                                        text = "CUADERNO DE AUTOR",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            letterSpacing = 1.2.sp,
                                            fontWeight = FontWeight.SemiBold,
                                        ),
                                        color = RojoCereza,
                                    )
                                    Text(
                                        text = "${lista.recetas.size} recetas propias",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = TextoSecundario,
                                    )
                                }
                                HorizontalDivider(color = BordeEditorial)
                            }
                        }
                    }

                    items(lista.recetas, key = { it.id }) { receta ->
                        val estadoLike = likesPorReceta[receta.id] ?: (receta.likesCount to receta.leDiLike)
                        val esFav = if (favoritosPorReceta.isNotEmpty()) {
                            favoritosPorReceta.contains(receta.id)
                        } else {
                            receta.esFavorito
                        }
                        TarjetaRecetaCatalogo(
                            receta = receta,
                            onClick = { onVerDetalle(receta.id) },
                            likesCount = estadoLike.first,
                            leDiLike = estadoLike.second,
                            onLikeClick = { onLikeClick(receta.id) },
                            mostrarFavorito = state.estaAutenticado,
                            esFavorito = esFav,
                            onFavoritoClick = { onFavoritoClick(receta.id) },
                            puedeEliminar = state.estaAutenticado && receta.esPropia && !esExplorar,
                            onEliminarClick = { onEliminarClick(receta) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EstadoVacioEditorial(
    esExplorar: Boolean,
    estaAutenticado: Boolean,
    onNuevaReceta: () -> Unit,
    onIniciarSesion: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(28.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Surface(
                shape = CircleShape,
                color = CremaCalida,
                border = BorderStroke(1.dp, BordeEditorial),
                modifier = Modifier.size(72.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.MenuBook,
                        contentDescription = null,
                        tint = RojoCereza,
                        modifier = Modifier.size(32.dp),
                    )
                }
            }

            Text(
                text = if (esExplorar) {
                    "Aún no hay recetas publicadas"
                } else {
                    "Tu recetario espera su primer platillo"
                },
                style = MaterialTheme.typography.headlineSmall,
                color = TextoPrincipal,
                textAlign = TextAlign.Center,
            )

            SeparadorOndulado(
                color = RosaFresa,
                modifier = Modifier.width(120.dp),
            )

            Text(
                text = when {
                    esExplorar -> "Cuando la comunidad publique platillos aparecerán en este catálogo."
                    estaAutenticado -> "Escribe los ingredientes, pasos y fotografía de tu primera creación."
                    else -> "Inicia sesión para redactar y organizar tu propio recetario."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = TextoSecundario,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(4.dp))

            if (estaAutenticado) {
                Button(
                    onClick = onNuevaReceta,
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = RojoCereza,
                        contentColor = CremaPapel,
                    ),
                    modifier = Modifier.defaultMinSize(minHeight = 48.dp),
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Crear primera receta", style = MaterialTheme.typography.labelLarge)
                }
            } else if (!esExplorar) {
                Button(
                    onClick = onIniciarSesion,
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = RojoCereza,
                        contentColor = CremaPapel,
                    ),
                    modifier = Modifier.defaultMinSize(minHeight = 48.dp),
                ) {
                    Text("Iniciar sesión", style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}

private val recetasDemoPreview = listOf(
    Receta(
        id = "p1",
        titulo = "Tarta tibia de fresas y crema",
        categoria = "postre",
        tiempoMinutos = 40,
        porciones = 6,
        ingredientes = listOf(Ingrediente("Fresas", "300", "g")),
        pasos = listOf("Hornear la masa y cubrir con crema."),
        esPropia = true,
    ),
    Receta(
        id = "p2",
        titulo = "Chilaquiles rojos caseros",
        categoria = "desayuno",
        tiempoMinutos = 20,
        porciones = 2,
        ingredientes = listOf(Ingrediente("Totopos", "200", "g")),
        pasos = listOf("Bañar en salsa de jitomate."),
        esPropia = false,
    ),
    Receta(
        id = "p3",
        titulo = "Pasta al limón con albahaca",
        categoria = "comida",
        tiempoMinutos = 25,
        porciones = 4,
        ingredientes = listOf(Ingrediente("Pasta", "400", "g")),
        pasos = listOf("Cocer y saltear con mantequilla y limón."),
        esPropia = false,
    ),
    Receta(
        id = "p4",
        titulo = "Panqué dorado de mantequilla",
        categoria = "postre",
        tiempoMinutos = 50,
        porciones = 8,
        ingredientes = listOf(Ingrediente("Mantequilla", "180", "g")),
        pasos = listOf("Acremar e incorporar harina."),
        esPropia = true,
    ),
)

@Preview(showBackground = true, showSystemUi = true, name = "Explorar · Visitante (Editorial)")
@Composable
private fun ExplorarVisitantePreview() {
    RecetarioTheme {
        HomeContent(
            state = HomeUiState(
                modo = ModoHome.EXPLORAR,
                estaAutenticado = false,
                lista = ListaUi.Contenido(recetasDemoPreview),
            ),
            likesPorReceta = mapOf(
                "p1" to (34 to true),
                "p2" to (19 to false),
                "p3" to (12 to false),
                "p4" to (27 to true),
            ),
            onNuevaReceta = {},
            onVerDetalle = {},
            onEliminarClick = {},
            onReintentar = {},
            onActualizar = {},
            onIrAExplorar = {},
            onIrAMisRecetas = {},
            onIniciarSesion = {},
            onCerrarSesion = {},
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Explorar · Registrado (Likes + Favoritos)")
@Composable
private fun ExplorarRegistradoPreview() {
    RecetarioTheme {
        HomeContent(
            state = HomeUiState(
                modo = ModoHome.EXPLORAR,
                estaAutenticado = true,
                nombreUsuario = "Ana Sofía",
                lista = ListaUi.Contenido(recetasDemoPreview),
            ),
            likesPorReceta = mapOf(
                "p1" to (34 to true),
                "p2" to (19 to false),
                "p3" to (12 to true),
                "p4" to (27 to false),
            ),
            favoritosPorReceta = setOf("p1", "p2"),
            onNuevaReceta = {},
            onVerDetalle = {},
            onEliminarClick = {},
            onReintentar = {},
            onActualizar = {},
            onIrAExplorar = {},
            onIrAMisRecetas = {},
            onIniciarSesion = {},
            onCerrarSesion = {},
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Mis recetas · Propias")
@Composable
private fun MisRecetasPreview() {
    RecetarioTheme {
        HomeContent(
            state = HomeUiState(
                modo = ModoHome.MIS_RECETAS,
                estaAutenticado = true,
                nombreUsuario = "Ana Sofía",
                lista = ListaUi.Contenido(recetasDemoPreview.filter { it.esPropia }),
            ),
            likesPorReceta = mapOf(
                "p1" to (34 to true),
                "p4" to (27 to false),
            ),
            favoritosPorReceta = setOf("p1"),
            onNuevaReceta = {},
            onVerDetalle = {},
            onEliminarClick = {},
            onReintentar = {},
            onActualizar = {},
            onIrAExplorar = {},
            onIrAMisRecetas = {},
            onIniciarSesion = {},
            onCerrarSesion = {},
        )
    }
}
