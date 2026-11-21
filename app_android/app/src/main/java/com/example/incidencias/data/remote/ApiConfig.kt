package com.example.incidencias.data.remote

import android.os.Build

object ApiConfig {
    // IP de la PC donde corre la API, en la red Wi-Fi (ver con "ipconfig" → Dirección IPv4).
    // Si el router le asigna otra IP a la PC, solo hay que cambiar esta línea.
    private const val IP_PC = "192.168.0.7"
    private const val PUERTO = 5000

    // Emulador: 10.0.2.2 apunta al localhost de la PC.
    // Teléfono físico: se usa la IP de la PC (deben estar en la misma red Wi-Fi).
    val BASE_URL: String =
        if (esEmulador()) "http://10.0.2.2:$PUERTO/" else "http://$IP_PC:$PUERTO/"

    // límites de las fotos según la API
    const val MAX_FOTO_REGISTRO_BYTES = 5L * 1024 * 1024
    const val MAX_FOTO_RESOLVER_BYTES = 10L * 1024 * 1024

    private fun esEmulador(): Boolean =
        Build.PRODUCT.startsWith("sdk") ||
            Build.HARDWARE.contains("ranchu") ||
            Build.HARDWARE.contains("goldfish") ||
            Build.FINGERPRINT.startsWith("generic")
}
