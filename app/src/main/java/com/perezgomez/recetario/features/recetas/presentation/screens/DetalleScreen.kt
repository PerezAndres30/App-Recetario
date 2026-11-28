package com.perezgomez.recetario.features.recetas.presentation.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.perezgomez.recetario.core.ui.components.BotonFavoritoReceta
import com.perezgomez.recetario.core.ui.components.BotonLikeReceta
import com.perezgomez.recetario.core.ui.components.ConfirmarEliminarDialog
import com.perezgomez.recetario.core.ui.components.ImagenReceta
import com.perezgomez.recetario.core.ui.components.PantallaCargando
import com.perezgomez.recetario.core.ui.components.PantallaError
import com.perezgomez.recetario.core.ui.components.SelloEditorial
import com.perezgomez.recetario.core.ui.components.SeparadorOndulado
import com.perezgomez.recetario.features.recetas.domain.entities.Ingrediente
import com.perezgomez.recetario.features.recetas.domain.entities.Receta
import com.perezgomez.recetario.features.recetas.domain.tts.EstadoLectura
import com.perezgomez.recetario.features.recetas.domain.tts.EtapaLectura
import com.perezgomez.recetario.features.recetas.presentation.viewmodels.DetalleViewModel
import com.perezgomez.recetario.ui.theme.AmarilloSuave
import com.perezgomez.recetario.ui.theme.BordeEditorial
import com.perezgomez.recetario.ui.theme.CremaCalida
import com.perezgomez.recetario.ui.theme.CremaPapel
import com.perezgomez.recetario.ui.theme.CremaVainilla
import com.perezgomez.recetario.ui.theme.RecetarioTheme
import com.perezgomez.recetario.ui.theme.RojoCereza
import com.perezgomez.recetario.ui.theme.RojoCerezaOscuro
import com.perezgomez.recetario.ui.theme.RosaFresa
import com.perezgomez.recetario.ui.theme.RosaFresaSuave
import com.perezgomez.recetario.ui.theme.TextoPrincipal
import com.perezgomez.recetario.ui.theme.TextoSecundario
import kotlinx.coroutines.launch

@Composable
fun DetalleScreen(
    viewModel: DetalleViewModel,
    mensajeNav: String?,
    onMensajeNavMostrado: () -> Unit,
    onVolver: () -> Unit,
    onEditar: (String) -> Unit,
    onEliminada: () -> Unit,
    estaAutenticado: Boolean = false,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { viewModel.cargar() }

    // Detener la locución al salir de la pantalla o enviarla a segundo plano
    LifecycleEventEffect(Lifecycle.Event.ON_STOP) { viewModel.detenerLectura() }
    DisposableEffect(Unit) {
        onDispose { viewModel.detenerLectura() }
    }

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

    LaunchedEffect(state.eliminada) {
        if (state.eliminada) onEliminada()
    }

    DetalleContent(
        state = state,
        snackbarHostState = snackbarHostState,
        onVolver = onVolver,
        onEditar = onEditar,
        onEliminarClick = viewModel::onEliminarClick,
        onReintentar = viewModel::cargar,
        onReproducirTts = viewModel::onReproducirTts,
        onContinuarTts = viewModel::onContinuarTts,
        onRepetirTts = viewModel::onRepetirTts,
        estaAutenticado = state.estaAutenticado,
        onLikeClick = viewModel::onToggleLike,
        onFavoritoClick = viewModel::onToggleFavorito,
    )

    val receta = (state.contenido as? DetalleContenido.Listo)?.receta
    if (state.confirmandoEliminar && receta != null) {
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
fun DetalleContent(
    state: DetalleUiState,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onVolver: () -> Unit,
    onEditar: (String) -> Unit,
    onEliminarClick: () -> Unit,
    onReintentar: () -> Unit,
    onReproducirTts: () -> Unit,
    onContinuarTts: () -> Unit,
    onRepetirTts: () -> Unit,
    estaAutenticado: Boolean = false,
    likesCount: Int? = null,
    leDiLike: Boolean? = null,
    esFavorito: Boolean? = null,
    onLikeClick: () -> Unit = {},
    onFavoritoClick: () -> Unit = {},
) {
    val receta = (state.contenido as? DetalleContenido.Listo)?.receta

    Scaffold(
        containerColor = CremaVainilla,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CremaVainilla,
                    titleContentColor = TextoPrincipal,
                ),
                navigationIcon = {
                    IconButton(
                        onClick = onVolver,
                        modifier = Modifier.defaultMinSize(minWidth = 48.dp, minHeight = 48.dp),
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = TextoPrincipal,
                        )
                    }
                },
                title = {
                    Text(
                        text = receta?.categoria?.uppercase() ?: "RECETARIO",
                        style = MaterialTheme.typography.labelMedium.copy(
                            letterSpacing = 1.4.sp,
                            fontWeight = FontWeight.SemiBold,
                        ),
                        color = RojoCereza,
                        maxLines = 1,
                    )
                },
                actions = {
                    if (state.esPropietarioVerificado && receta != null) {
                        IconButton(
                            onClick = { onEditar(receta.id) },
                            modifier = Modifier.defaultMinSize(minWidth = 48.dp, minHeight = 48.dp),
                        ) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = "Editar",
                                tint = TextoPrincipal,
                            )
                        }
                        IconButton(
                            onClick = onEliminarClick,
                            modifier = Modifier.defaultMinSize(minWidth = 48.dp, minHeight = 48.dp),
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Eliminar",
                                tint = MaterialTheme.colorScheme.error,
                            )
                        }
                    }
                },
            )
        },
    ) { padding ->
        when (val contenido = state.contenido) {
            DetalleContenido.Cargando -> PantallaCargando(Modifier.padding(padding))
            is DetalleContenido.Error -> PantallaError(
                mensaje = contenido.mensaje,
                onReintentar = onReintentar,
                modifier = Modifier.padding(padding),
            )

            is DetalleContenido.Listo -> DetalleCuerpo(
                receta = contenido.receta,
                ttsState = state.ttsState,
                estaAutenticado = estaAutenticado,
                likesCount = likesCount ?: contenido.receta.likesCount,
                leDiLike = leDiLike ?: contenido.receta.leDiLike,
                esFavorito = esFavorito ?: contenido.receta.esFavorito,
                onLikeClick = onLikeClick,
                onFavoritoClick = onFavoritoClick,
                onReproducirTts = onReproducirTts,
                onContinuarTts = onContinuarTts,
                onRepetirTts = onRepetirTts,
                modifier = Modifier.padding(padding),
            )
        }
    }
}

@Composable
private fun DetalleCuerpo(
    receta: Receta,
    ttsState: EstadoLectura,
    estaAutenticado: Boolean,
    likesCount: Int,
    leDiLike: Boolean,
    esFavorito: Boolean,
    onLikeClick: () -> Unit,
    onFavoritoClick: () -> Unit,
    onReproducirTts: () -> Unit,
    onContinuarTts: () -> Unit,
    onRepetirTts: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        // Portada fotográfica protagonista
        ImagenReceta(
            origen = receta.imagenUrl,
            descripcion = receta.titulo,
            cornerRadius = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(190.dp),
        )

        // Encabezado editorial de la página del recetario
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = receta.categoria.uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(
                        letterSpacing = 1.4.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                    color = RojoCereza,
                )
                if (receta.esPropia) {
                    SelloEditorial(
                        texto = "MI RECETA",
                        fondo = RosaFresaSuave,
                        textoColor = RojoCereza,
                    )
                }
            }

            Text(
                text = receta.titulo,
                style = MaterialTheme.typography.displayMedium,
                color = TextoPrincipal,
            )

            if (receta.descripcion.isNotBlank()) {
                Text(
                    text = receta.descripcion,
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextoSecundario,
                )
            }

            // Metadatos reales + acciones sociales
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ChipDatoCulinario(
                    icono = Icons.Default.Schedule,
                    texto = "${receta.tiempoMinutos} min",
                )
                ChipDatoCulinario(
                    icono = Icons.Default.Restaurant,
                    texto = "${receta.porciones} porc.",
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                BotonLikeReceta(
                    contador = likesCount,
                    seleccionado = leDiLike,
                    onClick = onLikeClick,
                    modoPildora = true,
                )
                if (estaAutenticado) {
                    BotonFavoritoReceta(
                        guardado = esFavorito,
                        onClick = onFavoritoClick,
                        mostrarTexto = true,
                    )
                }
            }
        }

        SeparadorOndulado(color = RosaFresa)

        // Bloque editorial de lectura asistida por voz
        SeccionLecturaVoz(
            ttsState = ttsState,
            onReproducir = onReproducirTts,
            onContinuar = onContinuarTts,
            onRepetir = onRepetirTts,
        )

        // Ingredientes estilo página impresa
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "02 INGREDIENTES",
                        style = MaterialTheme.typography.labelSmall.copy(
                            letterSpacing = 1.2.sp,
                            fontWeight = FontWeight.SemiBold,
                        ),
                        color = RojoCereza,
                    )
                    Text(
                        text = "Lo que necesitas",
                        style = MaterialTheme.typography.headlineSmall,
                        color = TextoPrincipal,
                    )
                }
                Text(
                    text = "${receta.ingredientes.size} elementos",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextoSecundario,
                )
            }

            Surface(
                shape = RoundedCornerShape(18.dp),
                color = CremaPapel,
                border = BorderStroke(1.dp, BordeEditorial),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                    receta.ingredientes.forEachIndexed { indice, ingrediente ->
                        val cantidad = listOf(ingrediente.cantidad, ingrediente.unidad)
                            .filter { it.isNotBlank() }
                            .joinToString(" ")
                        val texto = if (cantidad.isBlank()) {
                            ingrediente.nombre
                        } else {
                            "$cantidad de ${ingrediente.nombre}"
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(RojoCereza),
                            )
                            Text(
                                text = texto,
                                style = MaterialTheme.typography.bodyLarge,
                                color = TextoPrincipal,
                                modifier = Modifier.weight(1f),
                            )
                        }
                        if (indice < receta.ingredientes.lastIndex) {
                            HorizontalDivider(color = BordeEditorial.copy(alpha = 0.7f))
                        }
                    }
                }
            }
        }

        // Pasos de preparación enumerados como libro de cocina
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "03 PREPARACIÓN",
                    style = MaterialTheme.typography.labelSmall.copy(
                        letterSpacing = 1.2.sp,
                        fontWeight = FontWeight.SemiBold,
                    ),
                    color = RojoCereza,
                )
                Text(
                    text = "Paso a paso",
                    style = MaterialTheme.typography.headlineSmall,
                    color = TextoPrincipal,
                )
            }

            receta.pasos.forEachIndexed { indice, paso ->
                val enLectura = ttsState.etapa == EtapaLectura.PASO && ttsState.pasoActual == indice + 1
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = if (enLectura) RosaFresaSuave.copy(alpha = 0.55f) else CremaPapel,
                    border = BorderStroke(
                        width = if (enLectura) 1.5.dp else 1.dp,
                        color = if (enLectura) RojoCereza else BordeEditorial,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalAlignment = Alignment.Top,
                    ) {
                        Text(
                            text = (indice + 1).toString().padStart(2, '0'),
                            style = MaterialTheme.typography.headlineMedium,
                            color = if (enLectura) RojoCereza else RojoCereza.copy(alpha = 0.75f),
                        )
                        Column(
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.weight(1f),
                        ) {
                            if (enLectura) {
                                SelloEditorial(
                                    texto = "EN LECTURA",
                                    fondo = RojoCereza,
                                    textoColor = CremaVainilla,
                                )
                            }
                            Text(
                                text = paso,
                                style = MaterialTheme.typography.bodyLarge,
                                color = TextoPrincipal,
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
private fun ChipDatoCulinario(
    icono: ImageVector,
    texto: String,
) {
    Surface(
        shape = RoundedCornerShape(50),
        color = CremaCalida,
        border = BorderStroke(1.dp, BordeEditorial),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
        ) {
            Icon(
                imageVector = icono,
                contentDescription = null,
                tint = RojoCereza,
                modifier = Modifier.size(15.dp),
            )
            Text(
                text = texto,
                style = MaterialTheme.typography.labelLarge,
                color = TextoPrincipal,
            )
        }
    }
}

@Composable
private fun SeccionLecturaVoz(
    ttsState: EstadoLectura,
    onReproducir: () -> Unit,
    onContinuar: () -> Unit,
    onRepetir: () -> Unit,
) {
    val etiquetaEstado = when (ttsState.etapa) {
        EtapaLectura.INACTIVO -> "Lista"
        EtapaLectura.INTRODUCCION, EtapaLectura.PASO ->
            if (ttsState.estaReproduciendo) "Leyendo" else "En pausa"
        EtapaLectura.FINALIZADO -> "Finalizada"
    }

    val colorBadgeFondo = when (ttsState.etapa) {
        EtapaLectura.INACTIVO -> CremaCalida
        EtapaLectura.INTRODUCCION, EtapaLectura.PASO -> RojoCereza
        EtapaLectura.FINALIZADO -> AmarilloSuave
    }

    val colorBadgeTexto = when (ttsState.etapa) {
        EtapaLectura.INACTIVO -> TextoPrincipal
        EtapaLectura.INTRODUCCION, EtapaLectura.PASO -> CremaVainilla
        EtapaLectura.FINALIZADO -> RojoCerezaOscuro
    }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = CremaPapel,
        border = BorderStroke(1.dp, BordeEditorial),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = null,
                        tint = RojoCereza,
                        modifier = Modifier.size(18.dp),
                    )
                    Text(
                        text = "01 LECTURA POR VOZ ASISTIDA",
                        style = MaterialTheme.typography.labelSmall.copy(
                            letterSpacing = 1.sp,
                            fontWeight = FontWeight.Bold,
                        ),
                        color = RojoCereza,
                    )
                }
                SelloEditorial(
                    texto = etiquetaEstado,
                    fondo = colorBadgeFondo,
                    textoColor = colorBadgeTexto,
                )
            }

            Text(
                text = when (ttsState.etapa) {
                    EtapaLectura.INACTIVO -> "Presiona Reproducir para escuchar título e ingredientes"
                    EtapaLectura.INTRODUCCION -> "Leyendo introducción"
                    EtapaLectura.PASO -> "Paso ${ttsState.pasoActual} de ${ttsState.totalPasos}"
                    EtapaLectura.FINALIZADO -> "Receta completada"
                },
                style = MaterialTheme.typography.titleMedium,
                color = TextoPrincipal,
            )

            if (ttsState.etapa != EtapaLectura.INACTIVO) {
                Text(
                    text = when (ttsState.etapa) {
                        EtapaLectura.INTRODUCCION -> "Título, descripción e ingredientes"
                        EtapaLectura.PASO -> "Leyendo este paso..."
                        EtapaLectura.FINALIZADO -> "Todos los pasos fueron leídos"
                        EtapaLectura.INACTIVO -> ""
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = TextoSecundario,
                )
            }

            if (ttsState.estaActivo && ttsState.totalPasos > 0) {
                val progreso = when (ttsState.etapa) {
                    EtapaLectura.INTRODUCCION -> 0.1f
                    EtapaLectura.PASO -> (ttsState.pasoActual.toFloat() / ttsState.totalPasos.toFloat()).coerceIn(0.15f, 1f)
                    EtapaLectura.FINALIZADO -> 1f
                    EtapaLectura.INACTIVO -> 0f
                }
                LinearProgressIndicator(
                    progress = { progreso },
                    color = RojoCereza,
                    trackColor = RosaFresaSuave,
                    strokeCap = StrokeCap.Round,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(50)),
                )
            }

            if (ttsState.error != null) {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = ttsState.error,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(10.dp),
                    )
                }
            }

            val densidad = LocalDensity.current
            val paddingBotones = PaddingValues(horizontal = 8.dp, vertical = 10.dp)
            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val usarDosFilas = maxWidth < 300.dp || densidad.fontScale > 1.15f
                val textoReproducir = if (ttsState.esFinalizado) "Reiniciar" else "Reproducir"

                if (usarDosFilas) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        BotonTtsPrimario(
                            texto = textoReproducir,
                            icono = Icons.Default.PlayArrow,
                            onClick = onReproducir,
                            contentPadding = paddingBotones,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            BotonTtsSecundario(
                                texto = "Continuar",
                                icono = Icons.AutoMirrored.Filled.ArrowForward,
                                habilitado = ttsState.puedeContinuar,
                                onClick = onContinuar,
                                contentPadding = paddingBotones,
                                modifier = Modifier.weight(1f),
                            )
                            BotonTtsSecundario(
                                texto = "Repetir",
                                icono = Icons.Default.Replay,
                                habilitado = ttsState.puedeRepetir,
                                onClick = onRepetir,
                                contentPadding = paddingBotones,
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                } else {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        BotonTtsPrimario(
                            texto = textoReproducir,
                            icono = Icons.Default.PlayArrow,
                            onClick = onReproducir,
                            contentPadding = paddingBotones,
                            modifier = Modifier.weight(1.12f),
                        )
                        BotonTtsSecundario(
                            texto = "Continuar",
                            icono = Icons.AutoMirrored.Filled.ArrowForward,
                            habilitado = ttsState.puedeContinuar,
                            onClick = onContinuar,
                            contentPadding = paddingBotones,
                            modifier = Modifier.weight(1f),
                        )
                        BotonTtsSecundario(
                            texto = "Repetir",
                            icono = Icons.Default.Replay,
                            habilitado = ttsState.puedeRepetir,
                            onClick = onRepetir,
                            contentPadding = paddingBotones,
                            modifier = Modifier.weight(0.95f),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BotonTtsPrimario(
    texto: String,
    icono: ImageVector,
    onClick: () -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(50),
        colors = ButtonDefaults.buttonColors(
            containerColor = RojoCereza,
            contentColor = CremaVainilla,
        ),
        contentPadding = contentPadding,
        modifier = modifier.defaultMinSize(minHeight = 48.dp),
    ) {
        ContenidoBotonTts(
            icono = icono,
            texto = texto,
        )
    }
}

@Composable
private fun BotonTtsSecundario(
    texto: String,
    icono: ImageVector,
    habilitado: Boolean,
    onClick: () -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    OutlinedButton(
        onClick = onClick,
        enabled = habilitado,
        shape = RoundedCornerShape(50),
        border = BorderStroke(
            width = 1.dp,
            color = if (habilitado) RojoCereza else BordeEditorial,
        ),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = RojoCereza,
            disabledContentColor = TextoSecundario.copy(alpha = 0.55f),
        ),
        contentPadding = contentPadding,
        modifier = modifier.defaultMinSize(minHeight = 48.dp),
    ) {
        ContenidoBotonTts(
            icono = icono,
            texto = texto,
        )
    }
}

@Composable
private fun ContenidoBotonTts(
    icono: ImageVector,
    texto: String,
) {
    Icon(
        imageVector = icono,
        contentDescription = null,
        modifier = Modifier.size(16.dp),
    )
    Spacer(modifier = Modifier.width(4.dp))
    Text(
        text = texto,
        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
        maxLines = 1,
        softWrap = false,
    )
}

@Preview(showBackground = true, showSystemUi = true, name = "Detalle libro de cocina · Likes, Favorito y TTS")
@Composable
private fun DetalleScreenPreview() {
    RecetarioTheme {
        DetalleContent(
            state = DetalleUiState(
                esPropietarioVerificado = true,
                contenido = DetalleContenido.Listo(
                    receta = Receta(
                        id = "1",
                        titulo = "Huevos revueltos con hierbas",
                        descripcion = "Un desayuno clásico de cocina cálida, suave y lleno de aroma.",
                        categoria = "desayuno",
                        tiempoMinutos = 10,
                        porciones = 2,
                        ingredientes = listOf(
                            Ingrediente("Huevos orgánicos", "3", "piezas"),
                            Ingrediente("Mantequilla", "1", "cda"),
                            Ingrediente("Sal y pimienta", "", ""),
                        ),
                        pasos = listOf(
                            "Batir los huevos en un tazón con sal y pimienta al gusto.",
                            "Calentar la sartén a fuego medio y derretir la mantequilla.",
                            "Verter los huevos y remover suavemente con una espátula hasta alcanzar el punto deseado.",
                        ),
                        esPropia = true,
                        creadaEn = "2026-11-20T10:00:00Z",
                    ),
                ),
                ttsState = EstadoLectura(
                    etapa = EtapaLectura.PASO,
                    pasoActual = 2,
                    totalPasos = 3,
                    estaReproduciendo = true,
                ),
            ),
            snackbarHostState = remember { SnackbarHostState() },
            estaAutenticado = true,
            likesCount = 29,
            leDiLike = true,
            esFavorito = true,
            onVolver = {},
            onEditar = {},
            onEliminarClick = {},
            onReintentar = {},
            onReproducirTts = {},
            onContinuarTts = {},
            onRepetirTts = {},
        )
    }
}
