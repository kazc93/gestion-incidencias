package com.example.incidencias

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.example.incidencias.util.pantallaCompletaOscura
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.incidencias.data.model.EstadoClave
import com.example.incidencias.data.model.IncidenciaDetalle
import com.example.incidencias.data.repository.IncidenciaRepository
import com.example.incidencias.data.repository.Resultado
import com.example.incidencias.util.FechaUtils
import com.example.incidencias.util.ImagenRemota
import com.example.incidencias.util.aparecer
import com.example.incidencias.util.pulso
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class DetalleActivity : AppCompatActivity() {

    private val repositorio = IncidenciaRepository()
    private lateinit var historialAdapter: HistorialAdapter
    private var idIncidencia = 0
    private var cargaJob: Job? = null
    private lateinit var rvHistorial: RecyclerView

    // para animar solo lo que cambió al recargar (p. ej. al volver de asignar)
    private var estadoMostrado: String? = null
    private var historialMostrado: List<com.example.incidencias.data.model.Historial>? = null
    private val fotosMostradas = mutableSetOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pantallaCompletaOscura()
        setContentView(R.layout.activity_detalle)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        idIncidencia = intent.getIntExtra("id", 0)
        findViewById<View>(R.id.btnVolver).setOnClickListener { finish() }

        rvHistorial = findViewById(R.id.rvHistorial)
        rvHistorial.layoutManager = LinearLayoutManager(this)
        historialAdapter = HistorialAdapter(emptyList())
        rvHistorial.adapter = historialAdapter

        // mientras llega la API se muestra lo que vino de la lista
        val estado = intent.getStringExtra("estado") ?: ""
        findViewById<TextView>(R.id.tvFolio).text = intent.getStringExtra("folio") ?: ""
        findViewById<TextView>(R.id.tvTitulo).text = intent.getStringExtra("titulo") ?: ""
        findViewById<TextView>(R.id.tvDetalle).text =
            "${intent.getStringExtra("tipo") ?: ""} • Prioridad ${intent.getStringExtra("prioridad") ?: ""}"
        findViewById<TextView>(R.id.tvFecha).text = "Reportado el: ${intent.getStringExtra("fecha") ?: ""}"
        findViewById<TextView>(R.id.tvDescripcion).text = "Cargando..."
        findViewById<TextView>(R.id.tvReporta).text = "Reportado por: —"
        findViewById<TextView>(R.id.tvResponsable).text = "—"
        aplicarEstilo(estado, estado.replace("_", " "))
        estadoMostrado = estado
        // la acción se habilita hasta tener el detalle real (se necesita el responsable)
        findViewById<Button>(R.id.btnAccion).isEnabled = false

        if (idIncidencia == 0) {
            mostrarMensaje("No se recibió la incidencia")
            finish()
        }
    }

    // se recarga al volver de asignar/iniciar/resolver para ver el nuevo estado
    override fun onResume() {
        super.onResume()
        if (idIncidencia != 0) cargarDetalle()
    }

    private fun cargarDetalle() {
        cargaJob?.cancel()
        cargaJob = lifecycleScope.launch {
            when (val r = repositorio.obtenerIncidencia(idIncidencia)) {
                is Resultado.Exito -> mostrar(r.data)
                is Resultado.Error -> {
                    findViewById<TextView>(R.id.tvDescripcion).text = "—"
                    mostrarMensaje(r.mensaje)
                }
            }
        }
    }

    private fun mostrar(d: IncidenciaDetalle) {
        findViewById<TextView>(R.id.tvFolio).text = d.folio
        findViewById<TextView>(R.id.tvTitulo).text = d.titulo
        findViewById<TextView>(R.id.tvDescripcion).text = d.descripcion?.takeIf { it.isNotBlank() } ?: "—"
        findViewById<TextView>(R.id.tvDetalle).text = "${d.tipo} • Prioridad ${d.prioridad}"
        findViewById<TextView>(R.id.tvReporta).text = "Reportado por: ${d.reporta}"
        findViewById<TextView>(R.id.tvFecha).text = "Reportado el: ${FechaUtils.formatearFecha(d.fechaRegistro)}"
        findViewById<TextView>(R.id.tvResponsable).text = d.responsable ?: "Sin responsable asignado"
        findViewById<TextView>(R.id.tvSolucion).text = d.solucion?.takeIf { it.isNotBlank() } ?: "—"

        aplicarEstilo(d.estadoClave, d.estado)
        if (d.estadoClave != estadoMostrado) {
            findViewById<View>(R.id.tvEstado).pulso() // el estado cambió: se resalta
            estadoMostrado = d.estadoClave
        }
        configurarAccion(d)

        val historial = d.historial.orEmpty()
        if (historial != historialMostrado) {
            historialAdapter.actualizar(historial)
            rvHistorial.scheduleLayoutAnimation()
            historialMostrado = historial
        }

        val fotos = d.fotos.orEmpty()
        cargarFoto(findViewById(R.id.ivFotoInicial), fotos.firstOrNull { it.tipoEvidencia == "INICIAL" }?.url)
        cargarFoto(findViewById(R.id.ivFotoFinal), fotos.firstOrNull { it.tipoEvidencia == "FINAL" }?.url)
    }

    private fun aplicarEstilo(estadoClave: String, textoEstado: String) {
        val tvEstado = findViewById<TextView>(R.id.tvEstado)
        tvEstado.text = textoEstado.uppercase()
        val (fondo, color) = when (estadoClave) {
            EstadoClave.ASIGNADA -> R.drawable.bg_estado_asignada to R.color.texto_asignada
            EstadoClave.EN_ATENCION -> R.drawable.bg_estado_atencion to R.color.texto_en_atencion
            EstadoClave.RESUELTA -> R.drawable.bg_estado_resuelta to R.color.texto_resuelta
            else -> R.drawable.bg_estado_pendiente to R.color.texto_pendiente
        }
        tvEstado.setBackgroundResource(fondo)
        tvEstado.setTextColor(getColor(color))

        val resuelta = estadoClave == EstadoClave.RESUELTA
        findViewById<View>(R.id.llFotoFinal).visibility = if (resuelta) View.VISIBLE else View.GONE
        findViewById<View>(R.id.llSolucion).visibility = if (resuelta) View.VISIBLE else View.GONE
    }

    private fun configurarAccion(d: IncidenciaDetalle) {
        val btnAccion = findViewById<Button>(R.id.btnAccion)
        val (texto, destino) = when (d.estadoClave) {
            EstadoClave.REGISTRADA, EstadoClave.PENDIENTE -> "ASIGNAR RESPONSABLE" to AsignarActivity::class.java
            EstadoClave.ASIGNADA -> "INICIAR ATENCIÓN" to IniciarActivity::class.java
            EstadoClave.EN_ATENCION -> "RESOLVER INCIDENCIA" to ResolverActivity::class.java
            else -> {
                btnAccion.visibility = View.GONE
                return
            }
        }
        btnAccion.visibility = View.VISIBLE
        btnAccion.isEnabled = true
        btnAccion.text = texto
        btnAccion.setOnClickListener {
            val intent = Intent(this, destino)
            intent.putExtra("id", d.id)
            intent.putExtra("folio", d.folio)
            intent.putExtra("titulo", d.titulo)
            intent.putExtra("responsable", d.responsable)
            d.responsableId?.let { intent.putExtra("responsableId", it) }
            startActivity(intent)
        }
    }

    private fun cargarFoto(imageView: ImageView, url: String?) {
        if (url.isNullOrBlank()) return
        if (url in fotosMostradas) return // ya se descargó en una carga anterior
        lifecycleScope.launch {
            ImagenRemota.descargar(url)?.let {
                imageView.setImageBitmap(it)
                imageView.aparecer(500)
                fotosMostradas.add(url)
            }
        }
    }

    private fun mostrarMensaje(texto: String) {
        Toast.makeText(this, texto, Toast.LENGTH_LONG).show()
    }
}
