package com.perezgomez.recetario.presentation.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.perezgomez.recetario.domain.model.Categorias
import com.perezgomez.recetario.domain.voice.VoiceRecognitionState
import com.perezgomez.recetario.presentation.components.CampoTexto
import com.perezgomez.recetario.presentation.components.PantallaCargando
import com.perezgomez.recetario.presentation.viewmodels.FormViewModel

@Composable
fun FormScreen(
    viewModel: FormViewModel,
    onVolver: () -> Unit,
    onGuardado: (String) -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    var campoPendientePermiso by remember { mutableStateOf<CampoDictado?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { concedido ->
        val campo = campoPendientePermiso
        campoPendientePermiso = null
        if (concedido) {
            if (campo != null) {
                viewModel.onToggleDictado(campo)
            }
        } else {
            viewModel.onPermisoMicrofonoDenegado()
        }
    }

    val onSolicitarDictado: (CampoDictado) -> Unit = { campo ->
        val escuchando = state.estadoDictado is VoiceRecognitionState.Escuchando
        if (state.campoDictadoActivo == campo && escuchando) {
            viewModel.onToggleDictado(campo)
        } else {
            val tienePermiso = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO,
            ) == PackageManager.PERMISSION_GRANTED

            if (tienePermiso) {
                viewModel.onToggleDictado(campo)
            } else {
                campoPendientePermiso = campo
                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.onCancelarDictado()
        }
    }

    LaunchedEffect(state.guardado) {
        if (state.guardado) onGuardado(state.mensajeExito)
    }

    LaunchedEffect(state.mensajeDictado) {
        state.mensajeDictado?.let { mensaje ->
            snackbarHostState.showSnackbar(mensaje)
            viewModel.onMensajeDictadoConsumido()
        }
    }

    FormContent(
        state = state,
        snackbarHostState = snackbarHostState,
        onVolver = onVolver,
        onTituloChange = viewModel::onTituloChange,
        onDescripcionChange = viewModel::onDescripcionChange,
        onCategoriaChange = viewModel::onCategoriaChange,
        onTiempoChange = viewModel::onTiempoChange,
        onPorcionesChange = viewModel::onPorcionesChange,
        onFavoritaChange = viewModel::onFavoritaChange,
        onIngredienteChange = viewModel::onIngredienteChange,
        onAgregarIngrediente = viewModel::onAgregarIngrediente,
        onQuitarIngrediente = viewModel::onQuitarIngrediente,
        onPasoChange = viewModel::onPasoChange,
        onAgregarPaso = viewModel::onAgregarPaso,
        onQuitarPaso = viewModel::onQuitarPaso,
        onGuardar = viewModel::onGuardar,
        onSolicitarDictado = onSolicitarDictado,
        onDetenerDictado = viewModel::onDetenerDictado,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormContent(
    state: FormUiState,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onVolver: () -> Unit,
    onTituloChange: (String) -> Unit,
    onDescripcionChange: (String) -> Unit,
    onCategoriaChange: (String) -> Unit,
    onTiempoChange: (String) -> Unit,
    onPorcionesChange: (String) -> Unit,
    onFavoritaChange: (Boolean) -> Unit,
    onIngredienteChange: (Int, IngredienteForm) -> Unit,
    onAgregarIngrediente: () -> Unit,
    onQuitarIngrediente: (Int) -> Unit,
    onPasoChange: (Int, String) -> Unit,
    onAgregarPaso: () -> Unit,
    onQuitarPaso: (Int) -> Unit,
    onGuardar: () -> Unit,
    onSolicitarDictado: (CampoDictado) -> Unit = {},
    onDetenerDictado: () -> Unit = {},
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
                title = {
                    Text(
                        text = if (state.esEdicion) "Editar receta" else "Nueva receta",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
            )
        },
    ) { padding ->
        if (state.cargando) {
            PantallaCargando(Modifier.padding(padding))
            return@Scaffold
        }

        val errores = state.errores
        val habilitado = !state.guardando
        val estaEscuchando = state.estadoDictado is VoiceRecognitionState.Escuchando

        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Indicador de dictado por voz activo (cápsula verde salvia)
            if (estaEscuchando) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surface,
                            modifier = Modifier.size(36.dp),
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Mic,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(20.dp),
                                )
                            }
                        }
                        Text(
                            text = "Escuchando voz... Habla ahora",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.weight(1f),
                        )
                        TextButton(
                            onClick = onDetenerDictado,
                            shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = MaterialTheme.colorScheme.primary,
                            ),
                        ) {
                            Text(
                                text = "Detener",
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }
                }
            }

            // ================= 01 INFORMACIÓN GENERAL =================
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "01 INFORMACIÓN GENERAL",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                    ),
                    color = MaterialTheme.colorScheme.primary,
                )

                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        CampoTexto(
                            valor = state.titulo,
                            onValorChange = onTituloChange,
                            etiqueta = "Título",
                            error = errores.titulo,
                            habilitado = habilitado,
                        )

                        val escuchandoDescripcion = state.campoDictadoActivo == CampoDictado.Descripcion && estaEscuchando
                        CampoTexto(
                            valor = state.descripcion,
                            onValorChange = onDescripcionChange,
                            etiqueta = "Descripción (opcional)",
                            error = errores.descripcion,
                            unaLinea = false,
                            lineasMinimas = 2,
                            habilitado = habilitado,
                            trailingIcon = {
                                IconButton(
                                    onClick = { onSolicitarDictado(CampoDictado.Descripcion) },
                                    enabled = habilitado,
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Mic,
                                        contentDescription = if (escuchandoDescripcion) "Detener dictado" else "Dictar descripción",
                                        tint = if (escuchandoDescripcion) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                                    )
                                }
                            },
                        )

                        Text(
                            text = "Categoría",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Categorias.todas.forEach { categoria ->
                                FilterChip(
                                    selected = state.categoria == categoria,
                                    onClick = { onCategoriaChange(categoria) },
                                    label = { Text(categoria) },
                                    shape = RoundedCornerShape(20.dp),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                        containerColor = MaterialTheme.colorScheme.surface,
                                        labelColor = MaterialTheme.colorScheme.onSurface,
                                    ),
                                    border = FilterChipDefaults.filterChipBorder(
                                        enabled = habilitado,
                                        selected = state.categoria == categoria,
                                        borderColor = MaterialTheme.colorScheme.outlineVariant,
                                        selectedBorderColor = MaterialTheme.colorScheme.primary,
                                    ),
                                    enabled = habilitado,
                                )
                            }
                        }
                        if (errores.categoria != null) {
                            Text(
                                text = errores.categoria,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            CampoTexto(
                                valor = state.tiempoMinutos,
                                onValorChange = onTiempoChange,
                                etiqueta = "Tiempo (min)",
                                error = errores.tiempo,
                                tipoTeclado = KeyboardType.Number,
                                habilitado = habilitado,
                                modifier = Modifier.weight(1f),
                            )
                            CampoTexto(
                                valor = state.porciones,
                                onValorChange = onPorcionesChange,
                                etiqueta = "Porciones",
                                error = errores.porciones,
                                tipoTeclado = KeyboardType.Number,
                                habilitado = habilitado,
                                modifier = Modifier.weight(1f),
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 4.dp),
                        ) {
                            Text(
                                text = "Receta favorita",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f),
                            )
                            Switch(
                                checked = state.favorita,
                                onCheckedChange = onFavoritaChange,
                                enabled = habilitado,
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                                    checkedTrackColor = MaterialTheme.colorScheme.primary,
                                ),
                            )
                        }
                    }
                }
            }

            // ================= 02 INGREDIENTES =================
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "02 INGREDIENTES",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                    ),
                    color = MaterialTheme.colorScheme.primary,
                )

                state.ingredientes.forEachIndexed { indice, ingrediente ->
                    val escuchandoIngrediente = state.campoDictadoActivo == CampoDictado.IngredienteNombre(indice) && estaEscuchando
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                CampoTexto(
                                    valor = ingrediente.nombre,
                                    onValorChange = { onIngredienteChange(indice, ingrediente.copy(nombre = it)) },
                                    etiqueta = "Ingrediente ${indice + 1}",
                                    habilitado = habilitado,
                                    modifier = Modifier.weight(1f),
                                    trailingIcon = {
                                        IconButton(
                                            onClick = { onSolicitarDictado(CampoDictado.IngredienteNombre(indice)) },
                                            enabled = habilitado,
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Mic,
                                                contentDescription = if (escuchandoIngrediente) "Detener dictado" else "Dictar ingrediente",
                                                tint = if (escuchandoIngrediente) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                                            )
                                        }
                                    },
                                )
                                IconButton(
                                    onClick = { onQuitarIngrediente(indice) },
                                    enabled = habilitado && state.ingredientes.size > 1,
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Quitar ingrediente",
                                        tint = if (state.ingredientes.size > 1) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.outline,
                                    )
                                }
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                CampoTexto(
                                    valor = ingrediente.cantidad,
                                    onValorChange = { onIngredienteChange(indice, ingrediente.copy(cantidad = it)) },
                                    etiqueta = "Cantidad",
                                    habilitado = habilitado,
                                    modifier = Modifier.weight(1f),
                                )
                                CampoTexto(
                                    valor = ingrediente.unidad,
                                    onValorChange = { onIngredienteChange(indice, ingrediente.copy(unidad = it)) },
                                    etiqueta = "Unidad",
                                    habilitado = habilitado,
                                    modifier = Modifier.weight(1f),
                                )
                            }
                        }
                    }
                }

                if (errores.ingredientes != null) {
                    Text(
                        text = errores.ingredientes,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }

                OutlinedButton(
                    onClick = onAgregarIngrediente,
                    enabled = habilitado,
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = "+ Agregar ingrediente",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                    )
                }
            }

            // ================= 03 PASOS DE PREPARACIÓN =================
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "03 PASOS DE PREPARACIÓN",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                    ),
                    color = MaterialTheme.colorScheme.primary,
                )

                state.pasos.forEachIndexed { indice, paso ->
                    val escuchandoPaso = state.campoDictadoActivo == CampoDictado.Paso(indice) && estaEscuchando
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            // Indicador circular numerado
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .padding(top = 12.dp)
                                    .size(28.dp),
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "${indice + 1}",
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    )
                                }
                            }

                            CampoTexto(
                                valor = paso,
                                onValorChange = { onPasoChange(indice, it) },
                                etiqueta = "Paso ${indice + 1}",
                                unaLinea = false,
                                lineasMinimas = 2,
                                habilitado = habilitado,
                                modifier = Modifier.weight(1f),
                                trailingIcon = {
                                    IconButton(
                                        onClick = { onSolicitarDictado(CampoDictado.Paso(indice)) },
                                        enabled = habilitado,
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Mic,
                                            contentDescription = if (escuchandoPaso) "Detener dictado" else "Dictar paso",
                                            tint = if (escuchandoPaso) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                                        )
                                    }
                                },
                            )

                            IconButton(
                                onClick = { onQuitarPaso(indice) },
                                enabled = habilitado && state.pasos.size > 1,
                                modifier = Modifier.padding(top = 8.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Quitar paso",
                                    tint = if (state.pasos.size > 1) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.outline,
                                )
                            }
                        }
                    }
                }

                if (errores.pasos != null) {
                    Text(
                        text = errores.pasos,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }

                OutlinedButton(
                    onClick = onAgregarPaso,
                    enabled = habilitado,
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = "+ Agregar paso",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                    )
                }
            }

            if (state.error != null) {
                Text(
                    text = state.error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Botón principal
            Button(
                onClick = onGuardar,
                enabled = habilitado,
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
            ) {
                if (state.guardando) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                } else {
                    Text(
                        text = if (state.esEdicion) "Guardar cambios" else "Guardar receta",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
