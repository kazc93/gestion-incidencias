package com.example.incidencias

data class Incidencia(
    val folio: String,
    val titulo: String,
    val tipo: String,
    val prioridad: String,
    val estado: String,
    val fecha: String
)