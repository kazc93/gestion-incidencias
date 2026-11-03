package com.example.incidencias

import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.widget.Button
import android.widget.Spinner
import android.widget.Toast

class IniciarActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_iniciar)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val spEmpleado = findViewById<Spinner>(R.id.spEmpleado)

        findViewById<Button>(R.id.btnConfirmarInicio).setOnClickListener {
            if (spEmpleado.selectedItem == null) {
                Toast.makeText(this, "Seleccione quién inicia la atención", Toast.LENGTH_SHORT).show()
            } else {
                //espera el api
                Toast.makeText(this, "Datos válidos. Falta conectar el API", Toast.LENGTH_SHORT).show()
            }
        }

        findViewById<TextView>(R.id.tvFolio).text = intent.getStringExtra("folio") ?: ""
        findViewById<TextView>(R.id.tvTitulo).text = intent.getStringExtra("titulo") ?: ""
        findViewById<TextView>(R.id.tvResponsable).text = "Responsable: —"
    }
}