package com.perezgomez.recetario.core.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.perezgomez.recetario.features.recetas.domain.entities.Receta
import com.perezgomez.recetario.ui.theme.AmarilloMantequilla
import com.perezgomez.recetario.ui.theme.AmarilloSuave
import com.perezgomez.recetario.ui.theme.BordeEditorial
import com.perezgomez.recetario.ui.theme.CremaCalida
import com.perezgomez.recetario.ui.theme.CremaPapel
import com.perezgomez.recetario.ui.theme.CremaVainilla
import com.perezgomez.recetario.ui.theme.Durazno
import com.perezgomez.recetario.ui.theme.RojoCereza
import com.perezgomez.recetario.ui.theme.RosaFresa
import com.perezgomez.recetario.ui.theme.RosaFresaSuave
import com.perezgomez.recetario.ui.theme.TextoPrincipal

/** Separador ondulado fino y elegante para transiciones editoriales. */
@Composable
fun SeparadorOndulado(
    modifier: Modifier = Modifier,
    color: Color = RosaFresa,
    grosor: Dp = 1.8.dp,
    amplitud: Dp = 3.dp,
    longitudOnda: Dp = 20.dp,
) {
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(amplitud * 2 + grosor),
    ) {
        val amp = amplitud.toPx()
        val wave = longitudOnda.toPx()
        val cy = size.height / 2f
        val path = Path().apply {
            moveTo(0f, cy)
            var x = 0f
            while (x < size.width) {
                quadraticTo(x + wave * 0.25f, cy - amp, x + wave * 0.5f, cy)
                quadraticTo(x + wave * 0.75f, cy + amp, x + wave, cy)
                x += wave
            }
        }
        drawPath(
            path = path,
            color = color,
            style = Stroke(width = grosor.toPx(), cap = StrokeCap.Round),
        )
    }
}

/**
 * Banda Hero editorial oscura cálida (#302328) inspirada en la cabecera del catálogo de referencia:
 * aporta alto contraste fotográfico con tipografía Fraunces en crema vainilla y acentos durazno/cereza.
 */
@Composable
fun HeroCulinarioEditorial(
    etiquetaSuperior: String,
    titulo: String,
    subtitulo: String? = null,
    modifier: Modifier = Modifier,
    contenidoExtra: @Composable () -> Unit = {},
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clipToBounds()
            .background(TextoPrincipal),
    ) {
        // Detalle gráfico culinario sutil de mesa editorial (líneas finas en crema/durazno al 10%)
        Canvas(modifier = Modifier.matchParentSize()) {
            drawCircle(
                color = Durazno.copy(alpha = 0.14f),
                radius = size.height * 0.65f,
                center = Offset(size.width * 0.92f, size.height * 0.2f),
            )
            drawCircle(
                color = RojoCereza.copy(alpha = 0.22f),
                radius = size.height * 0.55f,
                center = Offset(size.width * 0.08f, size.height * 0.95f),
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = etiquetaSuperior.uppercase(),
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.2.sp),
                color = AmarilloMantequilla,
            )
            Text(
                text = titulo,
                style = MaterialTheme.typography.displaySmall,
                color = CremaVainilla,
            )
            if (!subtitulo.isNullOrBlank()) {
                Text(
                    text = subtitulo,
                    style = MaterialTheme.typography.bodyMedium,
                    color = CremaVainilla.copy(alpha = 0.85f),
                )
            }
            contenidoExtra()
        }
    }
}

/** Etiqueta editorial ligera y sobria. */
@Composable
fun SelloEditorial(
    texto: String,
    modifier: Modifier = Modifier,
    fondo: Color = CremaCalida,
    textoColor: Color = RojoCereza,
) {
    Surface(
        shape = RoundedCornerShape(50),
        color = fondo,
        modifier = modifier,
    ) {
        Text(
            text = texto,
            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.4.sp),
            color = textoColor,
            maxLines = 1,
            softWrap = false,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
        )
    }
}

/**
 * Encabezado editorial limpio estilo cabecera de revista gastronómica.
 */
@Composable
fun EncabezadoEditorial(
    titulo: String,
    subtitulo: String?,
    acciones: @Composable () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(CremaPapel)
            .statusBarsPadding(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 18.dp, end = 8.dp, top = 10.dp, bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = titulo,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (!subtitulo.isNullOrBlank()) {
                    Text(
                        text = subtitulo,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            acciones()
        }
        HorizontalDivider(color = BordeEditorial, thickness = 1.dp)
    }
}

/**
 * Botón compacto de Like con corazón rojo cereza y contador público (área táctil >= 48 dp).
 */
@Composable
fun BotonLikeReceta(
    contador: Int,
    seleccionado: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    modoPildora: Boolean = false,
) {
    val etiqueta = if (seleccionado) {
        "Quitar me gusta, $contador me gusta"
    } else {
        "Dar me gusta, $contador me gusta"
    }

    if (modoPildora) {
        Surface(
            shape = RoundedCornerShape(50),
            color = if (seleccionado) RosaFresaSuave else CremaCalida,
            border = BorderStroke(
                width = 1.dp,
                color = if (seleccionado) RojoCereza.copy(alpha = 0.45f) else BordeEditorial,
            ),
            modifier = modifier
                .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                .clip(RoundedCornerShape(50))
                .clickable(role = Role.Button, onClick = onClick)
                .semantics { contentDescription = etiqueta },
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Icon(
                    imageVector = if (seleccionado) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = null,
                    tint = if (seleccionado) RojoCereza else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp),
                )
                Text(
                    text = "$contador me gusta",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = if (seleccionado) FontWeight.Bold else FontWeight.Medium,
                    ),
                    color = if (seleccionado) RojoCereza else MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    softWrap = false,
                )
            }
        }
    } else {
        Row(
            modifier = modifier
                .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                .clip(RoundedCornerShape(12.dp))
                .clickable(role = Role.Button, onClick = onClick)
                .semantics { contentDescription = etiqueta }
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Icon(
                imageVector = if (seleccionado) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                contentDescription = null,
                tint = if (seleccionado) RojoCereza else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp),
            )
            Text(
                text = contador.toString(),
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = if (seleccionado) FontWeight.Bold else FontWeight.Medium,
                ),
                color = if (seleccionado) RojoCereza else MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                softWrap = false,
            )
        }
    }
}

/**
 * Marcador de Favoritos exclusivo para usuarios autenticados (icono distinto del corazón, >= 48 dp).
 */
@Composable
fun BotonFavoritoReceta(
    guardado: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    mostrarTexto: Boolean = false,
) {
    val etiqueta = if (guardado) "Quitar de favoritos" else "Guardar en favoritos"
    if (mostrarTexto) {
        Surface(
            shape = RoundedCornerShape(50),
            color = if (guardado) AmarilloSuave else CremaCalida,
            border = BorderStroke(
                width = 1.dp,
                color = if (guardado) Durazno else BordeEditorial,
            ),
            modifier = modifier
                .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                .clip(RoundedCornerShape(50))
                .clickable(role = Role.Button, onClick = onClick)
                .semantics { contentDescription = etiqueta },
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Icon(
                    imageVector = if (guardado) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                    contentDescription = null,
                    tint = if (guardado) RojoCereza else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp),
                )
                Text(
                    text = if (guardado) "Guardada" else "Guardar",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = if (guardado) FontWeight.Bold else FontWeight.Medium,
                    ),
                    color = if (guardado) RojoCereza else MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    softWrap = false,
                )
            }
        }
    } else {
        IconButton(
            onClick = onClick,
            modifier = modifier.size(48.dp),
        ) {
            Icon(
                imageVector = if (guardado) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                contentDescription = etiqueta,
                tint = if (guardado) RojoCereza else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

/**
 * Tarjeta visual editorial estilo revista culinaria:
 * - Fotografía grande protagonista arriba, sin bordes pesados ni stickers saturando la imagen.
 * - Categoría en mayúsculas pequeñas en rojo cereza sobre el título Fraunces.
 * - Línea inferior limpia con Like + contador, tiempo real, marcador de Favorito y eliminar opcional.
 */
@Composable
fun TarjetaRecetaCatalogo(
    receta: Receta,
    onClick: () -> Unit,
    likesCount: Int,
    leDiLike: Boolean,
    onLikeClick: () -> Unit,
    mostrarFavorito: Boolean,
    esFavorito: Boolean,
    onFavoritoClick: () -> Unit,
    puedeEliminar: Boolean,
    onEliminarClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
    ) {
        // Fotografía limpia de borde a borde
        ImagenReceta(
            origen = receta.imagenUrl,
            descripcion = "Foto de ${receta.titulo}",
            cornerRadius = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.08f),
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Categoría editorial en rojo cereza (estilo "MAJÍTÉ JEDLO" de la referencia)
        Text(
            text = buildString {
                append(receta.categoria.uppercase())
                if (receta.esPropia) append(" · TUYA")
            },
            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.7.sp),
            color = RojoCereza,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = receta.titulo,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )

        Spacer(modifier = Modifier.height(2.dp))

        // Meta real: tiempo y porciones
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Icon(
                imageVector = Icons.Default.Schedule,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(13.dp),
            )
            Text(
                text = "${receta.tiempoMinutos} min · ${receta.porciones} porc.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        // Fila inferior ligera con acciones de Like, Favorito (solo registrados) y Eliminar (solo propias)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                BotonLikeReceta(
                    contador = likesCount,
                    seleccionado = leDiLike,
                    onClick = onLikeClick,
                    modoPildora = false,
                )
                if (mostrarFavorito) {
                    BotonFavoritoReceta(
                        guardado = esFavorito,
                        onClick = onFavoritoClick,
                        mostrarTexto = false,
                    )
                }
            }

            if (puedeEliminar) {
                IconButton(
                    onClick = onEliminarClick,
                    modifier = Modifier.size(48.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Eliminar receta",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        }
    }
}

enum class DestinoBarraUsuario {
    EXPLORAR,
    MIS_RECETAS,
    FAVORITOS,
}

/**
 * Barra de navegación inferior limpia exclusiva para usuarios autenticados.
 */
@Composable
fun BarraNavegacionEditorial(
    destinoActual: DestinoBarraUsuario,
    onIrAExplorar: () -> Unit,
    onIrAMisRecetas: () -> Unit,
    onIrAFavoritos: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        color = CremaPapel,
        shadowElevation = 4.dp,
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            HorizontalDivider(color = BordeEditorial, thickness = 1.dp)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ItemNavegacionEditorial(
                    etiqueta = "Explorar",
                    icono = Icons.Default.Explore,
                    seleccionado = destinoActual == DestinoBarraUsuario.EXPLORAR,
                    onClick = onIrAExplorar,
                    modifier = Modifier.weight(1f),
                )
                ItemNavegacionEditorial(
                    etiqueta = "Mis recetas",
                    icono = Icons.AutoMirrored.Filled.MenuBook,
                    seleccionado = destinoActual == DestinoBarraUsuario.MIS_RECETAS,
                    onClick = onIrAMisRecetas,
                    modifier = Modifier.weight(1f),
                )
                ItemNavegacionEditorial(
                    etiqueta = "Favoritos",
                    icono = Icons.Default.Bookmark,
                    seleccionado = destinoActual == DestinoBarraUsuario.FAVORITOS,
                    onClick = onIrAFavoritos,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun ItemNavegacionEditorial(
    etiqueta: String,
    icono: ImageVector,
    seleccionado: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = if (seleccionado) RosaFresaSuave else Color.Transparent,
        modifier = modifier
            .padding(horizontal = 4.dp)
            .defaultMinSize(minHeight = 48.dp)
            .clip(RoundedCornerShape(14.dp))
            .clickable(role = Role.Tab, onClick = onClick),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                imageVector = icono,
                contentDescription = null,
                tint = if (seleccionado) RojoCereza else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp),
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = etiqueta,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = if (seleccionado) FontWeight.Bold else FontWeight.Medium,
                    letterSpacing = 0.sp,
                ),
                color = if (seleccionado) RojoCereza else MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
