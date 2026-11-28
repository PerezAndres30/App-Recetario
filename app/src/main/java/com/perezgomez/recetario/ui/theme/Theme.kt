package com.perezgomez.recetario.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = RojoCereza,
    onPrimary = CremaVainilla,
    primaryContainer = RosaFresa,
    onPrimaryContainer = RojoCerezaOscuro,
    secondary = Durazno,
    onSecondary = TextoPrincipal,
    secondaryContainer = AmarilloMantequilla,
    onSecondaryContainer = TextoPrincipal,
    tertiary = AzulTinta,
    onTertiary = CremaVainilla,
    tertiaryContainer = AzulPastel,
    onTertiaryContainer = AzulTinta,
    background = CremaVainilla,
    onBackground = TextoPrincipal,
    surface = CremaPapel,
    onSurface = TextoPrincipal,
    surfaceVariant = CremaCalida,
    onSurfaceVariant = TextoSecundario,
    outline = BordeEditorial,
    outlineVariant = BordeRosa,
    error = RojoError,
    onError = CremaPapel,
    errorContainer = RosaErrorFondo,
    onErrorContainer = RojoError,
)

private val DarkColorScheme = darkColorScheme(
    primary = RosaFresa,
    onPrimary = RojoCerezaOscuro,
    primaryContainer = RojoCereza,
    onPrimaryContainer = CremaVainilla,
    secondary = Durazno,
    onSecondary = TextoPrincipal,
    secondaryContainer = CacaoVariante,
    onSecondaryContainer = AmarilloMantequilla,
    tertiary = AzulPastel,
    onTertiary = AzulTinta,
    tertiaryContainer = AzulTinta,
    onTertiaryContainer = AzulPastelSuave,
    background = CacaoFondo,
    onBackground = CremaVainilla,
    surface = CacaoSuperficie,
    onSurface = CremaVainilla,
    surfaceVariant = CacaoVariante,
    onSurfaceVariant = RosaFresaSuave,
    outline = CacaoVariante,
    outlineVariant = RojoCereza,
    error = RosaFresa,
    onError = RojoCerezaOscuro,
    errorContainer = RojoCerezaOscuro,
    onErrorContainer = RosaFresaSuave,
)

@Composable
fun RecetarioTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    // Paleta editorial gastronómica fija para preservar la identidad visual del recetario.
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = RecetarioTypography,
        shapes = RecetarioShapes,
        content = content,
    )
}
