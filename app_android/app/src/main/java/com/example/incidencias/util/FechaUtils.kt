package com.example.incidencias.util

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Locale

object FechaUtils {

    /** "2026-11-21T08:15:40" (con o sin milisegundos) -> "21/11/2026" */
    fun formatearFecha(iso: String?): String = formatear(iso, "dd/MM/yyyy")

    /** "2026-11-21T08:15:40" -> "21/11/2026 08:15" */
    fun formatearFechaHora(iso: String?): String = formatear(iso, "dd/MM/yyyy HH:mm")

    private fun formatear(iso: String?, patron: String): String {
        if (iso.isNullOrBlank()) return "—"
        return try {
            // se toman solo los primeros 19 caracteres: yyyy-MM-ddTHH:mm:ss
            val entrada = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
            val fecha = entrada.parse(iso.take(19)) ?: return iso
            SimpleDateFormat(patron, Locale.getDefault()).format(fecha)
        } catch (e: ParseException) {
            iso
        }
    }
}
