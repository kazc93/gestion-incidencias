package com.example.incidencias.data.remote

import okhttp3.Interceptor
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    private val okHttp: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(LoggingSelectivo())
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS) // subir fotos puede tardar
            .build()
    }

    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(ApiConfig.BASE_URL)
            .client(okHttp)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    /**
     * Registra el body completo en Logcat (tag "okhttp.OkHttpClient"),
     * excepto en peticiones multipart, donde solo registra los encabezados
     * para no volcar los bytes de la foto.
     */
    private class LoggingSelectivo : Interceptor {
        private val conBody = HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)
        private val soloHeaders = HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.HEADERS)

        override fun intercept(chain: Interceptor.Chain): Response =
            if (chain.request().body is MultipartBody) soloHeaders.intercept(chain)
            else conBody.intercept(chain)
    }
}
