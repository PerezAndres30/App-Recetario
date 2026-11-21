package com.perezgomez.recetario.presentation.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
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
            viewModel.onDetenerDictado()
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
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(if (state.esEdicion) "Editar receta" else "Nueva receta") },
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
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            // Indicador de dictado por voz activo
            if (estaEscuchando) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                        )
                        Text(
                            text = "Escuchando voz... Habla ahora",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.weight(1f),
                        )
                        TextButton(onClick = onDetenerDictado) {
                            Text("Detener")
                        }
                    }
                }
            }

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

            Text("Categoría", style = MaterialTheme.typography.titleSmall)
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Categorias.todas.forEach { categoria ->
                    FilterChip(
                        selected = state.categoria == categoria,
                        onClick = { onCategoriaChange(categoria) },
                        label = { Text(categoria) },
                        enabled = habilitado,
                    )
                }
            }
            if (errores.categoria != null) {
                Text(errores.categoria, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Receta favorita", modifier = Modifier.weight(1f))
                Switch(checked = state.favorita, onCheckedChange = onFavoritaChange, enabled = habilitado)
            }

            // ---- Ingredientes ----
            Text("Ingredientes", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 8.dp))
            state.ingredientes.forEachIndexed { indice, ingrediente ->
                val escuchandoIngrediente = state.campoDictadoActivo == CampoDictado.IngredienteNombre(indice) && estaEscuchando
                Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Column(modifier = Modifier.weight(1f)) {
                        CampoTexto(
                            valor = ingrediente.nombre,
                            onValorChange = { onIngredienteChange(indice, ingrediente.copy(nombre = it)) },
                            etiqueta = "Ingrediente",
                            habilitado = habilitado,
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
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 4.dp)) {
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
                    IconButton(
                        onClick = { onQuitarIngrediente(indice) },
                        enabled = habilitado && state.ingredientes.size > 1,
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Quitar ingrediente")
                    }
                }
            }
            if (errores.ingredientes != null) {
                Text(errores.ingredientes, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }
            OutlinedButton(onClick = onAgregarIngrediente, enabled = habilitado) { Text("+ Agregar ingrediente") }

            // ---- Pasos ----
            Text("Pasos", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 8.dp))
            state.pasos.forEachIndexed { indice, paso ->
                val escuchandoPaso = state.campoDictadoActivo == CampoDictado.Paso(indice) && estaEscuchando
                Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
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
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Quitar paso")
                    }
                }
            }
            if (errores.pasos != null) {
                Text(errores.pasos, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }
            OutlinedButton(onClick = onAgregarPaso, enabled = habilitado) { Text("+ Agregar paso") }

            if (state.error != null) {
                Text(state.error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
            }

            Button(
                onClick = onGuardar,
                enabled = habilitado,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
            ) {
                if (state.guardando) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Text("Guardar")
                }
            }
        }
    }
}
