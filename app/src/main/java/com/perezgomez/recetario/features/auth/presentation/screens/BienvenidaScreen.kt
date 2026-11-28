package com.perezgomez.recetario.features.auth.presentation.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.perezgomez.recetario.R
import com.perezgomez.recetario.ui.theme.BordeEditorial
import com.perezgomez.recetario.ui.theme.CremaVainilla
import com.perezgomez.recetario.ui.theme.RecetarioTheme
import com.perezgomez.recetario.ui.theme.RojoCereza
import com.perezgomez.recetario.ui.theme.RosaFresa
import com.perezgomez.recetario.ui.theme.RosaFresaSuave
import com.perezgomez.recetario.ui.theme.TextoPrincipal
import com.perezgomez.recetario.ui.theme.TextoSecundario

/**
 * Portada editorial definitiva de App-Recetario, conforme al diseño aprobado en Figma:
 * - Cenefa superior de mantelería rosa a rayas con ondas festoneadas.
 * - Logotipo "recetario." en tipografía Fraunces rojo cereza.
 * - Fotografía de mesa culinaria con esquinas redondeadas.
 * - Título editorial a dos líneas: "La felicidad / se cocina."
 * - Subtítulo descriptivo en DM Sans.
 * - Botones cápsula: "Explorar como visitante" (Rojo cereza) e "Iniciar sesión" (Rosa fresa).
 * - Enlace directo "Crear cuenta".
 * - Soporte para banner de retorno seguro tras cierre de sesión (Vista 24 de Figma).
 */
@Composable
fun BienvenidaScreen(
    onExplorarVisitante: () -> Unit,
    onIniciarSesion: () -> Unit,
    onCrearCuenta: () -> Unit,
    mensajeAviso: String? = null,
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
            // Cenefa superior festoneada de rayas rosa y crema (Pattern / Mantelería Rosa)
            CenefaManteleriaFestoneada(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding(),
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // Logotipo editorial superior
                Text(
                    text = "recetario.",
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontSize = 34.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.5).sp,
                    ),
                    color = RojoCereza,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp),
                )

                // Banner de aviso opcional (Vista 24: Cierre de sesión seguro)
                if (mensajeAviso != null) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = RosaFresaSuave.copy(alpha = 0.8f),
                        border = BorderStroke(1.dp, RosaFresa),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Logout,
                                contentDescription = null,
                                tint = RojoCereza,
                                modifier = Modifier.size(18.dp),
                            )
                            Text(
                                text = mensajeAviso,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                ),
                                color = RojoCereza,
                            )
                        }
                    }
                }

                // Fotografía de mesa editorial protagonista
                Surface(
                    shape = RoundedCornerShape(22.dp),
                    border = BorderStroke(1.dp, BordeEditorial),
                    shadowElevation = 2.dp,
                    color = CremaVainilla,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp),
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.foto_bienvenida),
                        contentDescription = "Mesa de cocina editorial con frutas, pancakes y flores",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(22.dp)),
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Título editorial a dos líneas: "La felicidad / se cocina."
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(0.dp),
                ) {
                    Text(
                        text = "La felicidad",
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Normal,
                            letterSpacing = (-0.5).sp,
                        ),
                        color = RojoCereza,
                        textAlign = TextAlign.Center,
                    )
                    Text(
                        text = "se cocina.",
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Normal,
                            fontStyle = FontStyle.Italic,
                            letterSpacing = (-0.5).sp,
                        ),
                        color = RojoCereza,
                        textAlign = TextAlign.Center,
                    )
                }

                // Subtítulo descriptivo
                Text(
                    text = "Descubre, comparte y disfruta nuevas recetas a tu propio ritmo.",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 15.sp,
                        lineHeight = 22.sp,
                    ),
                    color = TextoSecundario,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Botón Primario: Cápsula Roja "Explorar como visitante"
                Button(
                    onClick = onExplorarVisitante,
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = RojoCereza,
                        contentColor = Color.White,
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                ) {
                    Text(
                        text = "Explorar como visitante",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp,
                        ),
                    )
                }

                // Botón Secundario: Cápsula Rosa "Iniciar sesión"
                Button(
                    onClick = onIniciarSesion,
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = RosaFresa,
                        contentColor = RojoCereza,
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                ) {
                    Text(
                        text = "Iniciar sesión",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp,
                        ),
                    )
                }

                // Enlace de texto: "Crear cuenta"
                TextButton(
                    onClick = onCrearCuenta,
                    modifier = Modifier.padding(top = 2.dp, bottom = 12.dp),
                ) {
                    Text(
                        text = "Crear cuenta",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp,
                            textDecoration = TextDecoration.Underline,
                        ),
                        color = RojoCereza,
                    )
                }
            }
        }
    }
}

/**
 * Cenefa de rayas rosa y crema con ondas festoneadas en la parte inferior,
 * representativa de los patrones de mantelería retro de Figma.
 */
@Composable
fun CenefaManteleriaFestoneada(
    modifier: Modifier = Modifier,
    altura: androidx.compose.ui.unit.Dp = 24.dp,
    colorRosa: Color = RosaFresa,
    colorBlanco: Color = Color.White,
) {
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(altura),
    ) {
        val stripeWidth = 14.dp.toPx()
        val numStripes = (size.width / stripeWidth).toInt() + 2

        // Dibujar rayas verticales alternadas
        for (i in 0 until numStripes) {
            val color = if (i % 2 == 0) colorRosa else colorBlanco
            drawRect(
                color = color,
                topLeft = Offset(i * stripeWidth, 0f),
                size = Size(stripeWidth, size.height - 4.dp.toPx()),
            )
        }

        // Ondas festoneadas en la base
        val festonRadius = stripeWidth / 2f
        val cy = size.height - 4.dp.toPx()
        for (i in 0 until numStripes) {
            val cx = i * stripeWidth + festonRadius
            val color = if (i % 2 == 0) colorRosa else colorBlanco
            val path = Path().apply {
                moveTo(cx - festonRadius, cy)
                quadraticTo(cx, cy + 5.dp.toPx(), cx + festonRadius, cy)
                close()
            }
            drawPath(path = path, color = color, style = Fill)
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun BienvenidaPreview() {
    RecetarioTheme {
        BienvenidaScreen(
            onExplorarVisitante = {},
            onIniciarSesion = {},
            onCrearCuenta = {},
        )
    }
}
