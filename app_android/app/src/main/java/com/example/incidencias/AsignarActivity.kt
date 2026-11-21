package com.example.incidencias

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import com.example.incidencias.util.pantallaCompletaOscura
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.incidencias.data.model.Catalogo
import com.example.incidencias.data.repository.IncidenciaRepository
import com.example.incidencias.data.repository.Resultado
import com.example.incidencias.util.idSeleccionado
import com.example.incidencias.util.llenarCatalogo
import com.example.incidencias.util.sacudir
import com.example.incidencias.util.seleccionarId
import kotlinx.coroutines.launch

class AsignarActivity : AppCompatActivity() {

    private val repositorio = IncidenciaRepository()
    private var idIncidencia = 0
    private var empleados: List<Catalogo> = emptyList()

    private lateinit var spResponsable: Spinner
    private lateinit var spAsigna: Spinner
    private lateinit var etComentario: EditText
    private lateinit var btnConfirmar: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pantallaCompletaOscura()
        setContentView(R.layout.activity_asignar)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // datos que llegaron desde el detalle
        idIncidencia = intent.getIntExtra("id", 0)
        findViewById<TextView>(R.id.tvFolio).text = intent.getStringExtra("folio") ?: ""
        findViewById<TextView>(R.id.tvTitulo).text = intent.getStringExtra("titulo") ?: ""

        spResponsable = findViewById(R.id.spResponsable)
        spAsigna = findViewById(R.id.spAsigna)
        etComentario = findViewById(R.id.etComentario)
        btnConfirmar = findViewById(R.id.btnConfirmarAsignacion)

        spResponsable.llenarCatalogo("Cargando...", emptyList())
        spAsigna.llenarCatalogo("Cargando...", emptyList())
        cargarEmpleados(savedInstanceState)

        btnConfirmar.setOnClickListener { confirmar() }
        findViewById<View>(R.id.btnVolver).setOnClickListener { finish() }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        spResponsable.idSeleccionado(empleados)?.let { outState.putInt("idResponsable", it) }
        spAsigna.idSeleccionado(empleados)?.let { outState.putInt("idAsigna", it) }
    }

    private fun cargarEmpleados(estado: Bundle?) {
        lifecycleScope.launch {
            when (val r = repositorio.obtenerEmpleados()) {
                is Resultado.Exito -> empleados = r.data
                is Resultado.Error -> mostrarMensaje(r.mensaje)
            }
            spResponsable.llenarCatalogo("Selecciona responsable", empleados)
            spAsigna.llenarCatalogo("Selecciona quién asigna", empleados)
            if (estado != null) {
                if (estado.containsKey("idResponsable")) spResponsable.seleccionarId(empleados, estado.getInt("idResponsable"))
                if (estado.containsKey("idAsigna")) spAsigna.seleccionarId(empleados, estado.getInt("idAsigna"))
            }
        }
    }

    private fun confirmar() {
        val idResponsable = spResponsable.idSeleccionado(empleados)
        val idAsigna = spAsigna.idSeleccionado(empleados)
        val comentario = etComentario.text.toString().trim()

        when {
            idIncidencia == 0 -> { mostrarMensaje("No se recibió la incidencia"); return }
            idResponsable == null -> { spResponsable.sacudir(); mostrarMensaje("Selecciona el responsable"); return }
            idAsigna == null -> { spAsigna.sacudir(); mostrarMensaje("Selecciona quién asigna"); return }
        }

        lifecycleScope.launch {
            ponerEnviando(true)
            try {
                when (val r = repositorio.asignarIncidencia(idIncidencia, idResponsable, idAsigna, comentario)) {
                    is Resultado.Exito -> {
                        mostrarMensaje(r.mensaje ?: "Incidencia asignada")
                        finish() // el detalle se recarga en onResume
                    }
                    is Resultado.Error -> mostrarMensaje(r.mensaje)
                }
            } finally {
                ponerEnviando(false)
            }
        }
    }

    private fun ponerEnviando(enviando: Boolean) {
        btnConfirmar.isEnabled = !enviando
        btnConfirmar.text = if (enviando) "ASIGNANDO..." else "CONFIRMAR ASIGNACIÓN"
    }

    private fun mostrarMensaje(texto: String) {
        Toast.makeText(this, texto, Toast.LENGTH_LONG).show()
    }
}
