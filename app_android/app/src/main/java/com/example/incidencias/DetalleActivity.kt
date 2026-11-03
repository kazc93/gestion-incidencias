package com.example.incidencias

import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.content.Intent
import android.view.View
import android.widget.Button
class DetalleActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_detalle)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // datos que llegaron desde la lista
        // el ?: "" significa: si no llegó nada, usar un texto vacío

        val folio = intent.getStringExtra("folio") ?: ""
        val titulo = intent.getStringExtra("titulo") ?: ""
        val tipo = intent.getStringExtra("tipo") ?: ""
        val prioridad = intent.getStringExtra("prioridad") ?: ""
        val estado = intent.getStringExtra("estado") ?: ""
        val fecha = intent.getStringExtra("fecha") ?: ""

        // se muestran en la pantalla
        findViewById<TextView>(R.id.tvFolio).text = folio
        findViewById<TextView>(R.id.tvTitulo).text = titulo
        findViewById<TextView>(R.id.tvEstado).text = estado.replace("_", " ")
        findViewById<TextView>(R.id.tvDetalle).text = "$tipo • Prioridad $prioridad"
        findViewById<TextView>(R.id.tvFecha).text = "Reportado el: $fecha"

        // estos datos vendrán de la API
        findViewById<TextView>(R.id.tvDescripcion).text = "—"
        findViewById<TextView>(R.id.tvReporta).text = "Reportado por: —"
        findViewById<TextView>(R.id.tvResponsable).text = "—"
        findViewById<TextView>(R.id.tvSolucion).text = "—"

        val tvEstado = findViewById<TextView>(R.id.tvEstado)
        val btnAccion = findViewById<Button>(R.id.btnAccion)

        when (estado) {
            "PENDIENTE" -> {
                tvEstado.setBackgroundResource(R.drawable.bg_estado_pendiente)
                tvEstado.setTextColor(getColor(R.color.texto_pendiente))
                btnAccion.text = "ASIGNAR RESPONSABLE"
                btnAccion.setOnClickListener {
                    val intent = Intent(this, AsignarActivity::class.java)
                    intent.putExtra("folio", folio)
                    intent.putExtra("titulo", titulo)
                    startActivity(intent)
                }
            }
            "ASIGNADA" -> {
                tvEstado.setBackgroundResource(R.drawable.bg_estado_asignada)
                tvEstado.setTextColor(getColor(R.color.texto_asignada))
                btnAccion.text = "INICIAR ATENCIÓN"
                btnAccion.setOnClickListener {
                    val intent = Intent(this, IniciarActivity::class.java)
                    intent.putExtra("folio", folio)
                    intent.putExtra("titulo", titulo)
                    startActivity(intent)
                }
            }
            "EN_ATENCION" -> {
                tvEstado.setBackgroundResource(R.drawable.bg_estado_atencion)
                tvEstado.setTextColor(getColor(R.color.texto_en_atencion))
                btnAccion.text = "RESOLVER INCIDENCIA"
                btnAccion.setOnClickListener {
                    val intent = Intent(this, ResolverActivity::class.java)
                    intent.putExtra("folio", folio)
                    intent.putExtra("titulo", titulo)
                    startActivity(intent)
                }
            }
            "RESUELTA" -> {
                tvEstado.setBackgroundResource(R.drawable.bg_estado_resuelta)
                tvEstado.setTextColor(getColor(R.color.texto_resuelta))
                btnAccion.visibility = View.GONE
                findViewById<View>(R.id.llFotoFinal).visibility = View.VISIBLE
                findViewById<View>(R.id.llSolucion).visibility = View.VISIBLE
            }
        }
    }
}