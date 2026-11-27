package com.perezgomez.recetario.features.recetas.presentation.screens

import androidx.compose.foundation.BorderStroke
import com.perezgomez.recetario.core.ui.components.ImagenReceta
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.perezgomez.recetario.features.recetas.domain.entities.Ingrediente
import com.perezgomez.recetario.features.recetas.domain.entities.Receta
import com.perezgomez.recetario.features.recetas.domain.tts.EstadoLectura
import com.perezgomez.recetario.features.recetas.domain.tts.EtapaLectura
import com.perezgomez.recetario.core.ui.components.ConfirmarEliminarDialog
import com.perezgomez.recetario.core.ui.components.PantallaCargando
import com.perezgomez.recetario.core.ui.components.PantallaError
import com.perezgomez.recetario.ui.theme.RecetarioTheme
import com.perezgomez.recetario.features.recetas.presentation.viewmodels.DetalleViewModel

@Composable
fun DetalleScreen(
    viewModel: DetalleViewModel,
    mensajeNav: String?,
    onMensajeNavMostrado: () -> Unit,
    onVolver: () -> Unit,
    onEditar: (String) -> Unit,
    onEliminada: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

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
    snackbarHostState: SnackbarHostState,
    onVolver: () -> Unit,
    onEditar: (String) -> Unit,
    onEliminarClick: () -> Unit,
    onReintentar: () -> Unit,
    onReproducirTts: () -> Unit,
    onContinuarTts: () -> Unit,
    onRepetirTts: () -> Unit,
) {
    val receta = (state.contenido as? DetalleContenido.Listo)?.receta

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
                title = {
                    Text(
                        text = "Receta",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                },
                actions = {
                    if (receta != null && state.esPropietarioVerificado) {
                        IconButton(onClick = { onEditar(receta.id) }) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Editar",
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        }
                        IconButton(onClick = onEliminarClick) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Eliminar",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
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
            is DetalleContenido.Listo -> RecetaCompleta(
                receta = contenido.receta,
                ttsState = state.ttsState,
                onReproducirTts = onReproducirTts,
                onContinuarTts = onContinuarTts,
                onRepetirTts = onRepetirTts,
                modifier = Modifier.padding(padding),
            )
        }
    }
}

@Composable
private fun RecetaCompleta(
    receta: Receta,
    ttsState: EstadoLectura,
    onReproducirTts: () -> Unit,
    onContinuarTts: () -> Unit,
    onRepetirTts: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Hero visual superior para fotografía
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp),
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                if (!receta.imagenUrl.isNullOrBlank()) {
                    ImagenReceta(
                        origen = receta.imagenUrl,
                        descripcion = "Foto de ${receta.titulo}",
                        modifier = Modifier.fillMaxSize(),
                    )
                } else Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.size(64.dp),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Restaurant,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(32.dp),
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Fotografía de la receta",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp),
                ) {
                    Text(
                        text = receta.categoria,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    )
                }
            }
        }

        // Título y descripción
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = receta.titulo,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            if (receta.descripcion.isNotBlank()) {
                Text(
                    text = receta.descripcion,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        // Metadatos
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
            ) {
                Text(
                    text = receta.categoria,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 4.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp),
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${receta.tiempoMinutos} min",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 4.dp),
            ) {
                Text(
                    text = "· ${receta.porciones} porc.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(modifier = Modifier.weight(1f))

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
            }
        }

        // ================= ASISTENTE TEXT-TO-SPEECH (TTS) =================
        SeccionLecturaVoz(
            ttsState = ttsState,
            onReproducir = onReproducirTts,
            onContinuar = onContinuarTts,
            onRepetir = onRepetirTts,
        )

        // ================= 02 INGREDIENTES =================
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "02 INGREDIENTES",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                ),
                color = MaterialTheme.colorScheme.primary,
            )

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    receta.ingredientes.forEach { ingrediente ->
                        val cantidad = listOf(ingrediente.cantidad, ingrediente.unidad)
                            .filter { it.isNotBlank() }
                            .joinToString(" ")
                        val texto = if (cantidad.isBlank()) ingrediente.nombre else "$cantidad de ${ingrediente.nombre}"
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(6.dp),
                            ) {}
                            Text(
                                text = texto,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }
                }
            }
        }

        // ================= 03 PASOS DE PREPARACIÓN =================
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "03 PASOS DE PREPARACIÓN",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                ),
                color = MaterialTheme.colorScheme.primary,
            )

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    receta.pasos.forEachIndexed { indice, paso ->
                        val enLectura = ttsState.etapa == EtapaLectura.PASO && ttsState.pasoActual == indice + 1

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (enLectura) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                            else MaterialTheme.colorScheme.surface,
                            border = if (enLectura) BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                verticalAlignment = Alignment.Top,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = if (enLectura) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primaryContainer,
                                    modifier = Modifier.size(26.dp),
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = "${indice + 1}",
                                            color = if (enLectura) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onPrimaryContainer,
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        )
                                    }
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = paso,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface,
                                    )
                                    if (enLectura) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Leyendo este paso...",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                            color = MaterialTheme.colorScheme.primary,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (receta.creadaEn.length >= 10) {
            Text(
                text = "Creada el ${receta.creadaEn.take(10)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}

/**
 * Tarjeta de control de lectura TextToSpeech.
 * Contiene los tres controles requeridos (Reproducir, Continuar, Repetir),
 * muestra el progreso del paso actual y el estado de finalización.
 */
@Composable
private fun SeccionLecturaVoz(
    ttsState: EstadoLectura,
    onReproducir: () -> Unit,
    onContinuar: () -> Unit,
    onRepetir: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = "01 LECTURA POR VOZ ASISTIDA",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
            ),
            color = MaterialTheme.colorScheme.primary,
        )

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (ttsState.estaReproduciendo) {
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
                } else {
                    MaterialTheme.colorScheme.surface
                },
            ),
            border = BorderStroke(
                width = if (ttsState.estaReproduciendo) 1.5.dp else 1.dp,
                color = if (ttsState.estaReproduciendo) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
            ),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Surface(
                        shape = CircleShape,
                        color = if (ttsState.estaReproduciendo) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(36.dp),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = "Lectura de receta",
                                tint = if (ttsState.estaReproduciendo) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = when (ttsState.etapa) {
                                EtapaLectura.INACTIVO -> "Lectura en voz alta"
                                EtapaLectura.INTRODUCCION -> "Leyendo introducción"
                                EtapaLectura.PASO -> "Paso ${ttsState.pasoActual} de ${ttsState.totalPasos}"
                                EtapaLectura.FINALIZADO -> "Receta completada"
                            },
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary,
                        )

                        Text(
                            text = when (ttsState.etapa) {
                                EtapaLectura.INACTIVO -> "Escucha el título, ingredientes y cada paso sin tocar la pantalla."
                                EtapaLectura.INTRODUCCION -> "Título, descripción e ingredientes. Presiona Continuar para el paso 1."
                                EtapaLectura.PASO -> "Presiona Continuar para avanzar o Repetir para volver a escuchar."
                                EtapaLectura.FINALIZADO -> "¡Has concluido la receta! Presiona Reproducir para reiniciar."
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = when {
                            ttsState.estaReproduciendo -> MaterialTheme.colorScheme.primary
                            ttsState.esFinalizado -> MaterialTheme.colorScheme.secondary
                            ttsState.estaActivo -> MaterialTheme.colorScheme.primaryContainer
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        },
                    ) {
                        Text(
                            text = when {
                                ttsState.estaReproduciendo -> "Leyendo"
                                ttsState.esFinalizado -> "Finalizada"
                                ttsState.estaActivo -> "En pausa"
                                else -> "Lista"
                            },
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = when {
                                ttsState.estaReproduciendo -> MaterialTheme.colorScheme.onPrimary
                                ttsState.esFinalizado -> MaterialTheme.colorScheme.onSecondary
                                ttsState.estaActivo -> MaterialTheme.colorScheme.onPrimaryContainer
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        )
                    }
                }

                if (ttsState.totalPasos > 0 && ttsState.estaActivo) {
                    val progreso = when (ttsState.etapa) {
                        EtapaLectura.INTRODUCCION -> 0.1f
                        EtapaLectura.PASO -> ttsState.pasoActual / ttsState.totalPasos.toFloat()
                        EtapaLectura.FINALIZADO -> 1f
                        EtapaLectura.INACTIVO -> 0f
                    }
                    LinearProgressIndicator(
                        progress = { progreso },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                    )
                }

                if (ttsState.error != null) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.errorContainer,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            text = ttsState.error,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(8.dp),
                        )
                    }
                }

                // Fila responsive con los 3 controles: Reproducir/Reiniciar, Continuar, Repetir
                val densidad = LocalDensity.current
                val paddingBotones = PaddingValues(horizontal = 8.dp, vertical = 10.dp)
                BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                    val usarDosFilas = maxWidth < 300.dp || densidad.fontScale > 1.15f
                    if (usarDosFilas) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                BotonReproducirTts(
                                    esFinalizado = ttsState.esFinalizado,
                                    habilitado = ttsState.etapa == EtapaLectura.INACTIVO || ttsState.etapa == EtapaLectura.FINALIZADO,
                                    onClick = onReproducir,
                                    contentPadding = paddingBotones,
                                    modifier = Modifier.weight(1f),
                                )
                                BotonSecundarioTts(
                                    texto = "Continuar",
                                    icono = Icons.AutoMirrored.Filled.ArrowForward,
                                    habilitado = ttsState.puedeContinuar,
                                    onClick = onContinuar,
                                    contentPadding = paddingBotones,
                                    modifier = Modifier.weight(1f),
                                )
                            }
                            BotonSecundarioTts(
                                texto = "Repetir",
                                icono = Icons.Default.Replay,
                                habilitado = ttsState.puedeRepetir,
                                onClick = onRepetir,
                                contentPadding = paddingBotones,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            BotonReproducirTts(
                                esFinalizado = ttsState.esFinalizado,
                                habilitado = ttsState.etapa == EtapaLectura.INACTIVO || ttsState.etapa == EtapaLectura.FINALIZADO,
                                onClick = onReproducir,
                                contentPadding = paddingBotones,
                                modifier = Modifier.weight(1.1f),
                            )
                            BotonSecundarioTts(
                                texto = "Continuar",
                                icono = Icons.AutoMirrored.Filled.ArrowForward,
                                habilitado = ttsState.puedeContinuar,
                                onClick = onContinuar,
                                contentPadding = paddingBotones,
                                modifier = Modifier.weight(1.05f),
                            )
                            BotonSecundarioTts(
                                texto = "Repetir",
                                icono = Icons.Default.Replay,
                                habilitado = ttsState.puedeRepetir,
                                onClick = onRepetir,
                                contentPadding = paddingBotones,
                                modifier = Modifier.weight(0.85f),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BotonReproducirTts(
    esFinalizado: Boolean,
    habilitado: Boolean,
    onClick: () -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = onClick,
        enabled = habilitado,
        contentPadding = contentPadding,
        modifier = modifier,
    ) {
        ContenidoBotonTts(
            icono = Icons.Default.PlayArrow,
            texto = if (esFinalizado) "Reiniciar" else "Reproducir",
        )
    }
}

@Composable
private fun BotonSecundarioTts(
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
        contentPadding = contentPadding,
        modifier = modifier,
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
        style = MaterialTheme.typography.labelMedium,
        maxLines = 1,
        softWrap = false,
    )
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun DetalleScreenPreview() {
    RecetarioTheme {
        DetalleContent(
            state = DetalleUiState(
                contenido = DetalleContenido.Listo(
                    receta = Receta(
                        id = "1",
                        titulo = "Huevos revueltos",
                        descripcion = "Un desayuno clásico, rápido y nutritivo para comenzar el día.",
                        categoria = "desayuno",
                        tiempoMinutos = 10,
                        porciones = 2,
                        ingredientes = listOf(
                            Ingrediente("Huevos", "3", "piezas"),
                            Ingrediente("Mantequilla", "1", "cda"),
                            Ingrediente("Sal y pimienta", "", ""),
                        ),
                        pasos = listOf(
                            "Batir los huevos en un tazón con sal y pimienta al gusto.",
                            "Calentar la sartén a fuego medio y derretir la mantequilla.",
                            "Verter los huevos y remover suavemente con una espátula hasta alcanzar el punto deseado.",
                        ),
                        favorita = true,
                        creadaEn = "2026-11-20T10:00:00Z",
                    )
                ),
                ttsState = EstadoLectura(
                    etapa = EtapaLectura.PASO,
                    pasoActual = 2,
                    totalPasos = 3,
                    estaReproduciendo = true,
                ),
            ),
            snackbarHostState = remember { SnackbarHostState() },
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
