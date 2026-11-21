package com.example.incidencias.util

import android.graphics.Color
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge

/**
 * Edge-to-edge con íconos claros en las barras del sistema.
 * La app siempre es oscura, así que no se deja que el modo claro del teléfono
 * ponga íconos oscuros (invisibles sobre nuestro fondo).
 */
fun ComponentActivity.pantallaCompletaOscura() {
    enableEdgeToEdge(
        statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
        navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT)
    )
}
