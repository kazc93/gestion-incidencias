package com.example.incidencias.util

import android.widget.ArrayAdapter
import android.widget.Spinner
import com.example.incidencias.R
import com.example.incidencias.data.model.Catalogo

// Spinners llenados con catálogos de la API.
// La posición 0 siempre es un texto inicial ("Selecciona...") que no es una opción válida.

fun Spinner.llenarCatalogo(textoInicial: String, opciones: List<Catalogo>) {
    val nombres = listOf(textoInicial) + opciones.map { it.nombre }
    val spAdapter = ArrayAdapter(context, R.layout.item_spinner, nombres)
    spAdapter.setDropDownViewResource(R.layout.item_spinner_dropdown)
    adapter = spAdapter
}

/** id del catálogo elegido, o null si sigue en el texto inicial */
fun Spinner.idSeleccionado(opciones: List<Catalogo>): Int? =
    opciones.getOrNull(selectedItemPosition - 1)?.id

fun Spinner.seleccionarId(opciones: List<Catalogo>, id: Int?) {
    if (id == null) return
    val indice = opciones.indexOfFirst { it.id == id }
    if (indice >= 0) setSelection(indice + 1)
}
