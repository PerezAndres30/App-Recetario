package com.perezgomez.recetario.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = VerdeBosque,
    onPrimary = Blanco,
    primaryContainer = VerdeSalvia,
    onPrimaryContainer = VerdeBosqueOscuro,
    secondary = MelocotonOscuro,
    onSecondary = Blanco,
    secondaryContainer = Melocoton,
    onSecondaryContainer = GrisTextoPrincipal,
    tertiary = VerdeBosqueOscuro,
    onTertiary = Blanco,
    background = FondoArena,
    onBackground = GrisTextoPrincipal,
    surface = Blanco,
    onSurface = GrisTextoPrincipal,
    surfaceVariant = FondoArena,
    onSurfaceVariant = GrisTextoSecundario,
    outline = GrisBorde,
    outlineVariant = VerdeSalviaBorde,
    error = RojoAlerta,
    onError = Blanco,
    errorContainer = RosaAlerta,
    onErrorContainer = RojoAlerta,
)

private val DarkColorScheme = darkColorScheme(
    primary = VerdeSalvia,
    onPrimary = VerdeBosqueOscuro,
    primaryContainer = VerdeBosque,
    onPrimaryContainer = Blanco,
    secondary = Melocoton,
    onSecondary = GrisTextoPrincipal,
    background = GrisTextoPrincipal,
    onBackground = FondoArena,
    surface = Color(0xFF262A27),
    onSurface = FondoArena,
    error = RojoAlerta,
    onError = Blanco,
    errorContainer = RosaAlerta,
    onErrorContainer = RojoAlerta,
)

@Composable
fun RecetarioTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    // Se utiliza la paleta personalizada de Figma y se evitan colores dinámicos
    // para preservar la identidad visual de la aplicación.
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = RecetarioTypography,
        content = content,
    )
}
