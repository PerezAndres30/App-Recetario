package com.perezgomez.recetario.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.perezgomez.recetario.presentation.components.CampoTexto
import com.perezgomez.recetario.presentation.viewmodels.LoginViewModel

@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onAutenticado: () -> Unit,
    onIrARegistro: () -> Unit,
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
    )
}

@Composable
fun LoginContent(
    state: LoginUiState,
    onCorreoChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onIniciarSesion: () -> Unit,
    onIrARegistro: () -> Unit,
) {
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("Recetario", style = MaterialTheme.typography.displaySmall)
            Text(
                "Inicia sesión para ver tus recetas",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 4.dp, bottom = 24.dp),
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
                modifier = Modifier.padding(top = 8.dp),
            )

            if (state.error != null) {
                Text(
                    text = state.error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                )
            }

            Button(
                onClick = onIniciarSesion,
                enabled = !state.cargando,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
                    .height(48.dp),
            ) {
                if (state.cargando) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Text("Iniciar sesión")
                }
            }

            TextButton(onClick = onIrARegistro, enabled = !state.cargando) {
                Text("¿No tienes cuenta? Regístrate")
            }
        }
    }
}
