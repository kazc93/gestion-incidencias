package com.example.incidencias.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.example.incidencias.data.remote.ApiConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

/** Descarga las fotos de evidencia que sirve la API. */
object ImagenRemota {

    private const val LADO_MAXIMO = 1280

    // cliente propio: el de Retrofit registra el body y volcaría los bytes de la imagen en Logcat
    private val cliente = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    /**
     * Acepta rutas relativas ("/uploads/x.jpg") o absolutas. Si la API devuelve
     * "localhost", se cambia por el host de BASE_URL (en el emulador localhost es el propio teléfono).
     */
    fun resolverUrl(url: String): String? {
        val base = ApiConfig.BASE_URL.toHttpUrl()
        val resuelta = base.resolve(url) ?: return null
        return if (resuelta.host == "localhost" || resuelta.host == "127.0.0.1") {
            resuelta.newBuilder().host(base.host).port(base.port).build().toString()
        } else {
            resuelta.toString()
        }
    }

    /** null si no se pudo descargar o decodificar */
    suspend fun descargar(url: String): Bitmap? = withContext(Dispatchers.IO) {
        try {
            val destino = resolverUrl(url) ?: return@withContext null
            cliente.newCall(Request.Builder().url(destino).build()).execute().use { r ->
                if (!r.isSuccessful) return@withContext null
                val bytes = r.body?.bytes() ?: return@withContext null
                val limites = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size, limites)
                var muestreo = 1
                while (maxOf(limites.outWidth, limites.outHeight) / (muestreo * 2) >= LADO_MAXIMO) muestreo *= 2
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size, BitmapFactory.Options().apply { inSampleSize = muestreo })
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            null
        }
    }
}
