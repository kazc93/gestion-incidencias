package com.example.incidencias

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // lista de incidencias
        val rvIncidencias = findViewById<RecyclerView>(R.id.rvIncidencias)
        rvIncidencias.layoutManager = LinearLayoutManager(this)
        rvIncidencias.adapter = IncidenciaAdapter(datosDePrueba())
    }

    // datos temporales, se reemplazan cuando se conecte la API
    private fun datosDePrueba(): List<Incidencia> {
        return listOf(
            Incidencia("INC-2026-00001", "Lámpara quemada", "Electricidad", "Media", "PENDIENTE", "20/10/2026"),
            Incidencia("INC-2026-00002", "Internet lento en oficina", "Red", "Alta", "ASIGNADA", "19/10/2026"),
            Incidencia("INC-2026-00003", "Computadora no enciende", "Equipo", "Crítica", "EN_ATENCION", "18/10/2026"),
            Incidencia("INC-2026-00004", "Silla rota", "Mobiliario", "Baja", "RESUELTA", "15/10/2026")
        )
    }
}