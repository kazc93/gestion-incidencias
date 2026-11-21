package com.example.incidencias.data.repository

/** Resultado de una llamada a la API, listo para la pantalla. */
sealed class Resultado<out T> {
    data class Exito<T>(val data: T, val mensaje: String?) : Resultado<T>()

    /** mensaje: texto para mostrar al usuario (viene del "message" de la API cuando existe) */
    data class Error(val mensaje: String, val codigoHttp: Int? = null) : Resultado<Nothing>()
}
