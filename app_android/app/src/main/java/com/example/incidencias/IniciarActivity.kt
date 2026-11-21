package com.example.incidencias

import android.os.Bundle
import android.view.View
import android.widget.Button
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

class IniciarActivity : AppCompatActivity() {

    private val repositorio = IncidenciaRepository()
    private var idIncidencia = 0
    private var empleados: List<Catalogo> = emptyList()

    private lateinit var spEmpleado: Spinner
    private lateinit var btnConfirmar: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pantallaCompletaOscura()
        setContentView(R.layout.activity_iniciar)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        idIncidencia = intent.getIntExtra("id", 0)
        findViewById<TextView>(R.id.tvFolio).text = intent.getStringExtra("folio") ?: ""
        findViewById<TextView>(R.id.tvTitulo).text = intent.getStringExtra("titulo") ?: ""
        findViewById<TextView>(R.id.tvResponsable).text =
            "Responsable: ${intent.getStringExtra("responsable") ?: "—"}"

        spEmpleado = findViewById(R.id.spEmpleado)
        btnConfirmar = findViewById(R.id.btnConfirmarInicio)
        spEmpleado.llenarCatalogo("Cargando...", emptyList())
        cargarEmpleados(savedInstanceState)

        btnConfirmar.setOnClickListener { confirmar() }
        findViewById<View>(R.id.btnVolver).setOnClickListener { finish() }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        spEmpleado.idSeleccionado(empleados)?.let { outState.putInt("idEmpleado", it) }
    }

    private fun cargarEmpleados(estado: Bundle?) {
        lifecycleScope.launch {
            when (val r = repositorio.obtenerEmpleados()) {
                is Resultado.Exito -> empleados = r.data
                is Resultado.Error -> mostrarMensaje(r.mensaje)
            }
            spEmpleado.llenarCatalogo("Selecciona empleado", empleados)
            // por defecto el responsable asignado; tras girar la pantalla, lo que se había elegido
            val idInicial = when {
                estado?.containsKey("idEmpleado") == true -> estado.getInt("idEmpleado")
                intent.hasExtra("responsableId") -> intent.getIntExtra("responsableId", 0)
                else -> null
            }
            spEmpleado.seleccionarId(empleados, idInicial)
        }
    }

    private fun confirmar() {
        val idEmpleado = spEmpleado.idSeleccionado(empleados)
        when {
            idIncidencia == 0 -> { mostrarMensaje("No se recibió la incidencia"); return }
            idEmpleado == null -> { spEmpleado.sacudir(); mostrarMensaje("Selecciona quién inicia la atención"); return }
        }

        lifecycleScope.launch {
            ponerEnviando(true)
            try {
                when (val r = repositorio.iniciarIncidencia(idIncidencia, idEmpleado)) {
                    is Resultado.Exito -> {
                        mostrarMensaje(r.mensaje ?: "Atención iniciada")
                        finish()
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
        btnConfirmar.text = if (enviando) "INICIANDO..." else "INICIAR ATENCIÓN"
    }

    private fun mostrarMensaje(texto: String) {
        Toast.makeText(this, texto, Toast.LENGTH_LONG).show()
    }
}
