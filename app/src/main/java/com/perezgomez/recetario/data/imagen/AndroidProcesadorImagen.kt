package com.perezgomez.recetario.data.imagen

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import com.perezgomez.recetario.domain.error.AppException
import com.perezgomez.recetario.domain.imagen.ImagenLista
import com.perezgomez.recetario.domain.imagen.ProcesadorImagen
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Singleton
class AndroidProcesadorImagen @Inject constructor(
    @ApplicationContext private val context: Context,
) : ProcesadorImagen {

    override suspend fun preparar(origen: String): Result<ImagenLista> = withContext(Dispatchers.IO) {
        try {
            val uri = Uri.parse(origen)
            val resolver = context.contentResolver

            // 1) Solo leer dimensiones, para decodificar ya reducida y no gastar memoria.
            val limites = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, limites) }
            if (limites.outWidth <= 0 || limites.outHeight <= 0) {
                return@withContext Result.failure(AppException("No se pudo leer la imagen elegida."))
            }
            val opciones = BitmapFactory.Options().apply {
                inSampleSize = factorMuestreo(limites.outWidth, limites.outHeight)
            }
            val decodificada = resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, opciones) }
                ?: return@withContext Result.failure(AppException("No se pudo leer la imagen elegida."))

            // 2) Corregir rotación (las fotos de cámara suelen venir "acostadas" con una marca EXIF).
            val orientacion = resolver.openInputStream(uri)?.use {
                ExifInterface(it).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
            } ?: ExifInterface.ORIENTATION_NORMAL
            val rotada = aplicarOrientacion(decodificada, orientacion)

            // 3) Ajuste final a 1600 px de lado largo y fondo blanco (JPEG no tiene transparencia).
            val escala = minOf(1f, ProcesadorImagen.LADO_MAXIMO_PX.toFloat() / maxOf(rotada.width, rotada.height))
            val ancho = (rotada.width * escala).toInt().coerceAtLeast(1)
            val alto = (rotada.height * escala).toInt().coerceAtLeast(1)
            val final = Bitmap.createBitmap(ancho, alto, Bitmap.Config.ARGB_8888)
            Canvas(final).apply {
                drawColor(Color.WHITE)
                drawBitmap(rotada, null, android.graphics.Rect(0, 0, ancho, alto), null)
            }

            // 4) Guardar como JPEG en caché.
            val carpeta = File(context.cacheDir, "fotos").apply { mkdirs() }
            val archivo = File(carpeta, "subir-${UUID.randomUUID()}.jpg")
            FileOutputStream(archivo).use { final.compress(Bitmap.CompressFormat.JPEG, ProcesadorImagen.CALIDAD_JPEG, it) }

            if (archivo.length() > ProcesadorImagen.BYTES_MAXIMOS) {
                archivo.delete()
                return@withContext Result.failure(AppException("La foto es demasiado pesada. Elige otra."))
            }
            Result.success(ImagenLista(ruta = archivo.absolutePath, bytes = archivo.length()))
        } catch (e: CancellationException) {
            throw e
        } catch (e: OutOfMemoryError) {
            Result.failure(AppException("La foto es demasiado grande para procesarla. Elige otra."))
        } catch (e: Exception) {
            Result.failure(AppException("No se pudo preparar la foto. Inténtalo con otra."))
        }
    }

    override fun descartar(ruta: String) {
        runCatching { File(ruta).delete() }
    }

    /** Potencia de 2 que deja el lado largo cerca (sin bajar de) del doble del máximo. */
    private fun factorMuestreo(ancho: Int, alto: Int): Int {
        var factor = 1
        val lado = maxOf(ancho, alto)
        while (lado / (factor * 2) >= ProcesadorImagen.LADO_MAXIMO_PX) factor *= 2
        return factor
    }

    private fun aplicarOrientacion(bitmap: Bitmap, orientacion: Int): Bitmap {
        val m = Matrix()
        when (orientacion) {
            ExifInterface.ORIENTATION_ROTATE_90 -> m.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> m.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> m.postRotate(270f)
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> m.postScale(-1f, 1f)
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> m.postScale(1f, -1f)
            ExifInterface.ORIENTATION_TRANSPOSE -> { m.postRotate(90f); m.postScale(-1f, 1f) }
            ExifInterface.ORIENTATION_TRANSVERSE -> { m.postRotate(270f); m.postScale(-1f, 1f) }
            else -> return bitmap
        }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, m, true)
    }
}
