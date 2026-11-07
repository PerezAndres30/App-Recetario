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
import com.perezgomez.recetario.presentation.viewmodels.RegisterViewModel

@Composable
fun RegisterScreen(
    viewModel: RegisterViewModel,
    onRegistrado: () -> Unit,
    onVolver: () -> Unit,
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
            Text("Crear cuenta", style = MaterialTheme.typography.headlineMedium)
            Text(
                "Regístrate para guardar tus recetas",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 4.dp, bottom = 24.dp),
            )

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
                modifier = Modifier.padding(top = 8.dp),
            )
            CampoTexto(
                valor = state.password,
                onValorChange = onPasswordChange,
                etiqueta = "Contraseña (mínimo 8 caracteres)",
                error = state.passwordError,
                esPassword = true,
                habilitado = !state.cargando,
                modifier = Modifier.padding(top = 8.dp),
            )
            CampoTexto(
                valor = state.confirmacion,
                onValorChange = onConfirmacionChange,
                etiqueta = "Confirmar contraseña",
                error = state.confirmacionError,
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
                onClick = onRegistrar,
                enabled = !state.cargando,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
                    .height(48.dp),
            ) {
                if (state.cargando) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Text("Registrarme")
                }
            }

            TextButton(onClick = onVolver, enabled = !state.cargando) {
                Text("¿Ya tienes cuenta? Inicia sesión")
            }
        }
    }
}
