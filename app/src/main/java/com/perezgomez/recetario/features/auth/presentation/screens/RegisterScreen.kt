package com.perezgomez.recetario.features.auth.presentation.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.perezgomez.recetario.core.ui.components.CampoTexto
import com.perezgomez.recetario.features.auth.presentation.viewmodels.RegisterViewModel
import com.perezgomez.recetario.ui.theme.BordeEditorial
import com.perezgomez.recetario.ui.theme.CremaPapel
import com.perezgomez.recetario.ui.theme.CremaVainilla
import com.perezgomez.recetario.ui.theme.RecetarioTheme
import com.perezgomez.recetario.ui.theme.RojoCereza
import com.perezgomez.recetario.ui.theme.TextoPrincipal
import com.perezgomez.recetario.ui.theme.TextoSecundario

@Composable
fun RegisterScreen(
    viewModel: RegisterViewModel,
    onRegistrado: () -> Unit,
    onVolver: () -> Unit,
    onContinuarComoVisitante: () -> Unit = {},
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(state.registrado) {
        if (state.registrado) onRegistrado()
    }

    RegisterContent(
        state = state,
        onNombreChange = viewModel::onNombreChange,
        onCorreoChange = viewModel::onCorreoChange,
        onPasswordChange = viewModel::onPasswordChange,
        onConfirmacionChange = viewModel::onConfirmacionChange,
        onRegistrar = viewModel::onRegistrar,
        onVolver = onVolver,
        onContinuarComoVisitante = onContinuarComoVisitante,
    )
}

@Composable
fun RegisterContent(
    state: RegisterUiState,
    onNombreChange: (String) -> Unit,
    onCorreoChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConfirmacionChange: (String) -> Unit,
    onRegistrar: () -> Unit,
    onVolver: () -> Unit,
    onContinuarComoVisitante: () -> Unit = {},
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = CremaVainilla,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding(),
        ) {
            // Cenefa festoneada superior (Pattern / Mantelería Rosa)
            CenefaManteleriaFestoneada(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding(),
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .imePadding()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // TopBar con botón Volver y logo
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    IconButton(
                        onClick = onVolver,
                        modifier = Modifier.size(36.dp),
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = RojoCereza,
                        )
                    }

                    Text(
                        text = "recetario.",
                        style = MaterialTheme.typography.displaySmall.copy(
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                        ),
                        color = RojoCereza,
                    )

                    Spacer(modifier = Modifier.size(36.dp))
                }

                Spacer(modifier = Modifier.height(2.dp))

                // Encabezado editorial: "Crea tu / recetario."
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Text(
                        text = "Crea tu",
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Normal,
                        ),
                        color = RojoCereza,
                        textAlign = TextAlign.Center,
                    )
                    Text(
                        text = "recetario.",
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Normal,
                            fontStyle = FontStyle.Italic,
                        ),
                        color = RojoCereza,
                        textAlign = TextAlign.Center,
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Guarda recetas con fotografía, escucha pasos y conserva favoritos.",
                        style = MaterialTheme.typography.bodyLarge.copy(fontSize = 15.sp),
                        color = TextoSecundario,
                        textAlign = TextAlign.Center,
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Tarjeta blanca de formulario
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, BordeEditorial),
                    shadowElevation = 2.dp,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        CampoTexto(
                            valor = state.nombre,
                            onValorChange = onNombreChange,
                            etiqueta = "Nombre",
                            error = state.nombreError,
                            habilitado = !state.cargando,
                        )

                        CampoTexto(
                            valor = state.correo,
                            onValorChange = onCorreoChange,
                            etiqueta = "Correo",
                            error = state.correoError,
                            tipoTeclado = KeyboardType.Email,
                            habilitado = !state.cargando,
                        )

                        CampoTexto(
                            valor = state.password,
                            onValorChange = onPasswordChange,
                            etiqueta = "Contraseña",
                            error = state.passwordError,
                            esPassword = true,
                            habilitado = !state.cargando,
                        )

                        CampoTexto(
                            valor = state.confirmacion,
                            onValorChange = onConfirmacionChange,
                            etiqueta = "Confirmar contraseña",
                            error = state.confirmacionError,
                            esPassword = true,
                            habilitado = !state.cargando,
                        )

                        if (state.error != null) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.errorContainer,
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Text(
                                    text = state.error,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(10.dp),
                                )
                            }
                        }

                        Button(
                            onClick = onRegistrar,
                            enabled = !state.cargando,
                            shape = RoundedCornerShape(50),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = RojoCereza,
                                contentColor = Color.White,
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                        ) {
                            if (state.cargando) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp,
                                    color = CremaVainilla,
                                )
                            } else {
                                Text(
                                    text = "Crear cuenta",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.SemiBold,
                                    ),
                                )
                            }
                        }
                    }
                }

                // Enlace "¿Ya tienes cuenta? Inicia sesión"
                TextButton(
                    onClick = onVolver,
                    enabled = !state.cargando,
                    modifier = Modifier.padding(bottom = 12.dp),
                ) {
                    Text(
                        text = "¿Ya tienes cuenta? Inicia sesión",
                        color = RojoCereza,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            textDecoration = TextDecoration.Underline,
                        ),
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun RegisterScreenPreview() {
    RecetarioTheme {
        RegisterContent(
            state = RegisterUiState(nombre = "Sofía", correo = "sofia@cocina.mx"),
            onNombreChange = {},
            onCorreoChange = {},
            onPasswordChange = {},
            onConfirmacionChange = {},
            onRegistrar = {},
            onVolver = {},
        )
    }
}
