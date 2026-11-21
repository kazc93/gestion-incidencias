package com.example.incidencias.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File

object ImagenUtils {

    private const val LADO_MAXIMO = 1920
    private const val CALIDAD_INICIAL = 80
    private const val CALIDAD_MINIMA = 40

    /**
     * Convierte una foto (cámara o galería) a un JPEG real comprimido (calidad ~80),
     * corrige la rotación EXIF y la reduce si es muy grande.
     * Devuelve un archivo temporal en cacheDir listo para enviar a la API.
     *
     * @throws IllegalArgumentException si no se puede leer o no cabe en maxBytes
     */
    suspend fun prepararFotoJpeg(context: Context, uri: Uri, maxBytes: Long): File = withContext(Dispatchers.IO) {
        val resolver = context.contentResolver

        // 1) leer solo dimensiones para calcular el factor de reducción
        val limites = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, limites) }
        if (limites.outWidth <= 0 || limites.outHeight <= 0) {
            throw IllegalArgumentException("No se pudo leer la imagen")
        }

        var muestra = 1
        while (limites.outWidth / (muestra * 2) >= LADO_MAXIMO || limites.outHeight / (muestra * 2) >= LADO_MAXIMO) {
            muestra *= 2
        }

        // 2) decodificar la imagen reducida
        val opciones = BitmapFactory.Options().apply { inSampleSize = muestra }
        var bitmap = resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, opciones) }
            ?: throw IllegalArgumentException("No se pudo leer la imagen")

        // 3) corregir rotación (las fotos de cámara suelen venir giradas por EXIF)
        val grados = resolver.openInputStream(uri)?.use { leerRotacion(ExifInterface(it)) } ?: 0
        if (grados != 0) {
            val matriz = Matrix().apply { postRotate(grados.toFloat()) }
            val rotado = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matriz, true)
            if (rotado != bitmap) bitmap.recycle()
            bitmap = rotado
        }

        // 4) comprimir a JPEG; si no cabe, bajar calidad
        var calidad = CALIDAD_INICIAL
        var bytes: ByteArray
        do {
            val salida = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, calidad, salida)
            bytes = salida.toByteArray()
            calidad -= 10
        } while (bytes.size > maxBytes && calidad >= CALIDAD_MINIMA)
        bitmap.recycle()

        if (bytes.size > maxBytes) {
            throw IllegalArgumentException("La foto supera el tamaño máximo permitido")
        }

        val archivo = File(context.cacheDir, "subida_${System.currentTimeMillis()}.jpg")
        archivo.writeBytes(bytes)
        archivo
    }

    private fun leerRotacion(exif: ExifInterface): Int =
        when (exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90
            ExifInterface.ORIENTATION_ROTATE_180 -> 180
            ExifInterface.ORIENTATION_ROTATE_270 -> 270
            else -> 0
        }
}
