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
import androidx.compose.material3.HorizontalDivider
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
import com.perezgomez.recetario.features.auth.presentation.viewmodels.LoginViewModel
import com.perezgomez.recetario.ui.theme.BordeEditorial
import com.perezgomez.recetario.ui.theme.CremaPapel
import com.perezgomez.recetario.ui.theme.CremaVainilla
import com.perezgomez.recetario.ui.theme.RecetarioTheme
import com.perezgomez.recetario.ui.theme.RojoCereza
import com.perezgomez.recetario.ui.theme.TextoPrincipal
import com.perezgomez.recetario.ui.theme.TextoSecundario

@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onAutenticado: () -> Unit,
    onIrARegistro: () -> Unit,
    onContinuarComoVisitante: () -> Unit = {},
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(state.autenticado) {
        if (state.autenticado) onAutenticado()
    }

    LoginContent(
        state = state,
        onCorreoChange = viewModel::onCorreoChange,
        onPasswordChange = viewModel::onPasswordChange,
        onIniciarSesion = viewModel::onIniciarSesion,
        onIrARegistro = onIrARegistro,
        onContinuarComoVisitante = onContinuarComoVisitante,
    )
}

@Composable
fun LoginContent(
    state: LoginUiState,
    onCorreoChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onIniciarSesion: () -> Unit,
    onIrARegistro: () -> Unit,
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
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                // TopBar con botón de regreso opcional y logo
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    IconButton(
                        onClick = onContinuarComoVisitante,
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

                Spacer(modifier = Modifier.height(4.dp))

                // Encabezado editorial: "Bienvenido / de vuelta."
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Text(
                        text = "Bienvenido",
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Normal,
                        ),
                        color = RojoCereza,
                        textAlign = TextAlign.Center,
                    )
                    Text(
                        text = "de vuelta.",
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Normal,
                            fontStyle = FontStyle.Italic,
                        ),
                        color = RojoCereza,
                        textAlign = TextAlign.Center,
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Inicia sesión para abrir tu cuaderno de cocina y favoritos.",
                        style = MaterialTheme.typography.bodyLarge.copy(fontSize = 15.sp),
                        color = TextoSecundario,
                        textAlign = TextAlign.Center,
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Tarjeta de formulario blanca editorial sobre fondo crema
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, BordeEditorial),
                    shadowElevation = 2.dp,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
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
                            onClick = onIniciarSesion,
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
                                    text = "Iniciar sesión",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.SemiBold,
                                    ),
                                )
                            }
                        }
                    }
                }

                // Enlaces de navegación inferior
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    TextButton(
                        onClick = onIrARegistro,
                        enabled = !state.cargando,
                    ) {
                        Text(
                            text = "¿No tienes cuenta? Regístrate",
                            color = RojoCereza,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                textDecoration = TextDecoration.Underline,
                            ),
                        )
                    }

                    OutlinedButton(
                        onClick = onContinuarComoVisitante,
                        enabled = !state.cargando,
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(1.dp, BordeEditorial),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = CremaPapel,
                            contentColor = TextoSecundario,
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp),
                    ) {
                        Text(
                            text = "Explorar como visitante",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                            ),
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun LoginScreenPreview() {
    RecetarioTheme {
        LoginContent(
            state = LoginUiState(correo = "sofia@cocina.mx"),
            onCorreoChange = {},
            onPasswordChange = {},
            onIniciarSesion = {},
            onIrARegistro = {},
        )
    }
}
