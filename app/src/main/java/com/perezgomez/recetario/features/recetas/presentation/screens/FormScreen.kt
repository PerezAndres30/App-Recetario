package com.perezgomez.recetario.features.recetas.presentation.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.perezgomez.recetario.core.ui.components.CampoTexto
import com.perezgomez.recetario.core.ui.components.PantallaCargando
import com.perezgomez.recetario.core.ui.components.SeccionFoto
import com.perezgomez.recetario.core.ui.components.SeparadorOndulado
import com.perezgomez.recetario.features.recetas.domain.entities.Categorias
import com.perezgomez.recetario.features.recetas.presentation.viewmodels.FormViewModel
import com.perezgomez.recetario.ui.theme.BordeEditorial
import com.perezgomez.recetario.ui.theme.CremaCalida
import com.perezgomez.recetario.ui.theme.CremaPapel
import com.perezgomez.recetario.ui.theme.CremaVainilla
import com.perezgomez.recetario.ui.theme.RecetarioTheme
import com.perezgomez.recetario.ui.theme.RojoCereza
import com.perezgomez.recetario.ui.theme.RosaFresa
import com.perezgomez.recetario.ui.theme.RosaFresaSuave
import com.perezgomez.recetario.ui.theme.TextoPrincipal
import com.perezgomez.recetario.ui.theme.TextoSecundario
import java.io.File
import java.util.UUID

@Composable
fun FormScreen(
    viewModel: FormViewModel,
    onVolver: () -> Unit,
    onGuardado: (String) -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    // Cámara: escribe en un archivo de la caché de la app (vía FileProvider); no pide permiso de cámara.
    var uriCamara by rememberSaveable { mutableStateOf<String?>(null) }
    val camara = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { tomada ->
        val uri = uriCamara
        uriCamara = null
        if (tomada && uri != null) viewModel.onFotoElegida(uri)
    }
    // Galería: selector de fotos del sistema; tampoco requiere permisos.
    val galeria = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) viewModel.onFotoElegida(uri.toString())
    }

    LaunchedEffect(state.guardado) {
        if (state.guardado) onGuardado(state.mensajeExito)
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
        onIngredienteChange = viewModel::onIngredienteChange,
        onAgregarIngrediente = viewModel::onAgregarIngrediente,
        onQuitarIngrediente = viewModel::onQuitarIngrediente,
        onPasoChange = viewModel::onPasoChange,
        onAgregarPaso = viewModel::onAgregarPaso,
        onQuitarPaso = viewModel::onQuitarPaso,
        onGuardar = viewModel::onGuardar,
        onTomarFoto = {
            try {
                val carpeta = File(context.cacheDir, "fotos").apply { mkdirs() }
                val archivo = File(carpeta, "camara-${UUID.randomUUID()}.jpg")
                val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", archivo)
                uriCamara = uri.toString()
                camara.launch(uri)
            } catch (e: Exception) {
                uriCamara = null
                viewModel.onFotoError("No se pudo abrir la cámara en este dispositivo.")
            }
        },
        onElegirDeGaleria = {
            try {
                galeria.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            } catch (e: Exception) {
                viewModel.onFotoError("No se pudo abrir la galería en este dispositivo.")
            }
        },
        onQuitarFoto = viewModel::onQuitarFoto,
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
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
    onIngredienteChange: (Int, IngredienteForm) -> Unit,
    onAgregarIngrediente: () -> Unit,
    onQuitarIngrediente: (Int) -> Unit,
    onPasoChange: (Int, String) -> Unit,
    onAgregarPaso: () -> Unit,
    onQuitarPaso: (Int) -> Unit,
    onGuardar: () -> Unit,
    onTomarFoto: () -> Unit = {},
    onElegirDeGaleria: () -> Unit = {},
    onQuitarFoto: () -> Unit = {},
) {
    val habilitado = !state.guardando && !state.procesandoImagen
    val focusManager = LocalFocusManager.current

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
                    Column {
                        Text(
                            text = "PÁGINA DE RECETARIO",
                            style = MaterialTheme.typography.labelSmall.copy(
                                letterSpacing = 1.2.sp,
                                fontWeight = FontWeight.SemiBold,
                            ),
                            color = RojoCereza,
                        )
                        Text(
                            text = if (state.esEdicion) "Editar receta" else "Nueva receta",
                            style = MaterialTheme.typography.headlineSmall,
                            color = TextoPrincipal,
                        )
                    }
                },
            )
        },
    ) { padding ->
        if (state.cargando) {
            PantallaCargando(Modifier.padding(padding))
        } else {
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .imePadding()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                // 01 · Portada del platillo
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SeccionCabeceraEditorial(
                        numero = "01",
                        titulo = "Portada y nombre del platillo",
                        subtitulo = "Fotografía protagonista y ficha principal",
                    )

                    SeccionFoto(
                        vistaPrevia = state.vistaPreviaImagen,
                        procesando = state.procesandoImagen,
                        habilitado = !state.guardando,
                        onTomarFoto = onTomarFoto,
                        onElegirDeGaleria = onElegirDeGaleria,
                        onQuitarFoto = onQuitarFoto,
                    )
                }

                // Título, descripción, categoría y porciones directamente sobre papel del recetario
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    CampoTexto(
                        valor = state.titulo,
                        onValorChange = onTituloChange,
                        etiqueta = "Título",
                        error = state.errores.titulo,
                        habilitado = habilitado,
                        textStyle = MaterialTheme.typography.headlineSmall.copy(
                            color = TextoPrincipal,
                        ),
                    )

                    CampoTexto(
                        valor = state.descripcion,
                        onValorChange = onDescripcionChange,
                        etiqueta = "Descripción (opcional)",
                        error = state.errores.descripcion,
                        unaLinea = false,
                        lineasMinimas = 2,
                        habilitado = habilitado,
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Categoría culinaria",
                            style = MaterialTheme.typography.labelLarge,
                            color = TextoPrincipal,
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Categorias.todas.forEach { cat ->
                                val seleccionada = state.categoria == cat
                                FilterChip(
                                    selected = seleccionada,
                                    onClick = {
                                        if (habilitado) {
                                            focusManager.clearFocus()
                                            onCategoriaChange(cat)
                                        }
                                    },
                                    enabled = habilitado,
                                    shape = RoundedCornerShape(50),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = RojoCereza,
                                        selectedLabelColor = CremaVainilla,
                                        containerColor = CremaPapel,
                                        labelColor = TextoPrincipal,
                                    ),
                                    border = FilterChipDefaults.filterChipBorder(
                                        enabled = habilitado,
                                        selected = seleccionada,
                                        borderColor = BordeEditorial,
                                        selectedBorderColor = RojoCereza,
                                    ),
                                    label = {
                                        Text(
                                            text = cat,
                                            style = MaterialTheme.typography.labelLarge,
                                        )
                                    },
                                )
                            }
                        }
                        if (state.errores.categoria != null) {
                            Text(
                                text = state.errores.categoria,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error,
                            )
                        }
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        CampoTexto(
                            valor = state.tiempoMinutos,
                            onValorChange = onTiempoChange,
                            etiqueta = "Tiempo (min)",
                            error = state.errores.tiempo,
                            tipoTeclado = KeyboardType.Number,
                            habilitado = habilitado,
                            modifier = Modifier.weight(1f),
                        )
                        CampoTexto(
                            valor = state.porciones,
                            onValorChange = onPorcionesChange,
                            etiqueta = "Porciones",
                            error = state.errores.porciones,
                            tipoTeclado = KeyboardType.Number,
                            habilitado = habilitado,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }

                SeparadorOndulado(color = RosaFresa)

                // 02 · Ingredientes (lista editorial continua sobre hoja del recetario)
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    SeccionCabeceraEditorial(
                        numero = "02",
                        titulo = "Ingredientes y medidas",
                        subtitulo = "${state.ingredientes.size} elementos en la despensa",
                    )

                    if (state.errores.ingredientes != null) {
                        Text(
                            text = state.errores.ingredientes,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }

                    state.ingredientes.forEachIndexed { indice, ing ->
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = CremaPapel,
                            border = BorderStroke(1.dp, BordeEditorial),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                ) {
                                    CampoTexto(
                                        valor = ing.nombre,
                                        onValorChange = { onIngredienteChange(indice, ing.copy(nombre = it)) },
                                        etiqueta = "Ingrediente ${indice + 1}",
                                        habilitado = habilitado,
                                        modifier = Modifier.weight(1f),
                                    )
                                    if (state.ingredientes.size > 1) {
                                        IconButton(
                                            onClick = { onQuitarIngrediente(indice) },
                                            enabled = habilitado,
                                            modifier = Modifier.defaultMinSize(minWidth = 48.dp, minHeight = 48.dp),
                                        ) {
                                            Icon(
                                                Icons.Default.Close,
                                                contentDescription = "Quitar ingrediente",
                                                tint = TextoSecundario,
                                            )
                                        }
                                    }
                                }

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth(),
                                ) {
                                    CampoTexto(
                                        valor = ing.cantidad,
                                        onValorChange = { onIngredienteChange(indice, ing.copy(cantidad = it)) },
                                        etiqueta = "Cant.",
                                        habilitado = habilitado,
                                        modifier = Modifier.weight(1f),
                                    )
                                    CampoTexto(
                                        valor = ing.unidad,
                                        onValorChange = { onIngredienteChange(indice, ing.copy(unidad = it)) },
                                        etiqueta = "Unidad",
                                        habilitado = habilitado,
                                        modifier = Modifier.weight(1f),
                                    )
                                }
                            }
                        }
                    }

                    OutlinedButton(
                        onClick = onAgregarIngrediente,
                        enabled = habilitado,
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(1.dp, RojoCereza),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = RojoCereza),
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = 48.dp),
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Agregar ingrediente",
                            style = MaterialTheme.typography.labelLarge,
                        )
                    }
                }

                HorizontalDivider(color = BordeEditorial)

                // 03 · Preparación paso a paso
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    SeccionCabeceraEditorial(
                        numero = "03",
                        titulo = "Preparación paso a paso",
                        subtitulo = "Describe cada etapa para guiar al cocinero y a la lectura por voz",
                    )

                    if (state.errores.pasos != null) {
                        Text(
                            text = state.errores.pasos,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }

                    state.pasos.forEachIndexed { indice, paso ->
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = CremaPapel,
                            border = BorderStroke(1.dp, BordeEditorial),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Row(
                                verticalAlignment = Alignment.Top,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.padding(14.dp),
                            ) {
                                Box(
                                    modifier = Modifier.padding(top = 10.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        text = (indice + 1).toString().padStart(2, '0'),
                                        style = MaterialTheme.typography.headlineSmall,
                                        color = RojoCereza,
                                    )
                                }
                                CampoTexto(
                                    valor = paso,
                                    onValorChange = { onPasoChange(indice, it) },
                                    etiqueta = "Paso ${indice + 1}",
                                    unaLinea = false,
                                    lineasMinimas = 2,
                                    habilitado = habilitado,
                                    modifier = Modifier.weight(1f),
                                )
                                if (state.pasos.size > 1) {
                                    IconButton(
                                        onClick = { onQuitarPaso(indice) },
                                        enabled = habilitado,
                                        modifier = Modifier.defaultMinSize(minWidth = 48.dp, minHeight = 48.dp),
                                    ) {
                                        Icon(
                                            Icons.Default.Close,
                                            contentDescription = "Quitar paso",
                                            tint = TextoSecundario,
                                        )
                                    }
                                }
                            }
                        }
                    }

                    OutlinedButton(
                        onClick = onAgregarPaso,
                        enabled = habilitado,
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(1.dp, RojoCereza),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = RojoCereza),
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = 48.dp),
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Agregar paso",
                            style = MaterialTheme.typography.labelLarge,
                        )
                    }
                }

                if (state.error != null) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.errorContainer,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            text = state.error,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(12.dp),
                        )
                    }
                }

                Button(
                    onClick = {
                        focusManager.clearFocus()
                        onGuardar()
                    },
                    enabled = habilitado,
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = RojoCereza,
                        contentColor = CremaVainilla,
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 54.dp),
                ) {
                    if (state.guardando) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = CremaVainilla,
                        )
                    } else {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (state.esEdicion) "Guardar cambios" else "Guardar receta",
                            style = MaterialTheme.typography.labelLarge,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
private fun SeccionCabeceraEditorial(
    numero: String,
    titulo: String,
    subtitulo: String,
) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = "$numero · RECETARIO",
            style = MaterialTheme.typography.labelSmall.copy(
                letterSpacing = 1.2.sp,
                fontWeight = FontWeight.Bold,
            ),
            color = RojoCereza,
        )
        Text(
            text = titulo,
            style = MaterialTheme.typography.headlineSmall,
            color = TextoPrincipal,
        )
        Text(
            text = subtitulo,
            style = MaterialTheme.typography.bodySmall,
            color = TextoSecundario,
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Página de recetario (Crear)")
@Composable
private fun FormScreenPreview() {
    RecetarioTheme {
        FormContent(
            state = FormUiState(
                titulo = "Panqué de limón",
                descripcion = "Esponjoso con glaseado cítrico.",
                categoria = "postre",
                tiempoMinutos = "45",
                porciones = "8",
                ingredientes = listOf(IngredienteForm("Harina de trigo", "250", "g")),
                pasos = listOf("Precalentar el horno a 180 °C y engrasar el molde."),
            ),
            onVolver = {},
            onTituloChange = {},
            onDescripcionChange = {},
            onCategoriaChange = {},
            onTiempoChange = {},
            onPorcionesChange = {},
            onIngredienteChange = { _, _ -> },
            onAgregarIngrediente = {},
            onQuitarIngrediente = {},
            onPasoChange = { _, _ -> },
            onAgregarPaso = {},
            onQuitarPaso = {},
            onGuardar = {},
        )
    }
}
