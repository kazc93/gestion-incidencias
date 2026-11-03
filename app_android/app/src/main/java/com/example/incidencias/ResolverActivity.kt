package com.example.incidencias

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.io.File

class ResolverActivity : AppCompatActivity() {
    private var uriFoto: Uri? = null
    private var archivoFoto: File? = null

    private val camara = registerForActivityResult(ActivityResultContracts.TakePicture()) { exito ->
        if (exito) {
            mostrarFoto()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_resolver)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        findViewById<TextView>(R.id.tvFolio).text = intent.getStringExtra("folio") ?: ""
        findViewById<TextView>(R.id.tvTitulo).text = intent.getStringExtra("titulo") ?: ""

        // botón de la cámara (igual que en el registro)
        findViewById<Button>(R.id.btnTomarFotoFinal).setOnClickListener {
            val carpeta = File(filesDir, "fotos")
            if (!carpeta.exists()) {
                carpeta.mkdirs()
            }
            val archivo = File(carpeta, "final_${System.currentTimeMillis()}.jpg")
            archivoFoto = archivo
            val uri = FileProvider.getUriForFile(this, "$packageName.fileprovider", archivo)
            uriFoto = uri
            camara.launch(uri)
        }

        // botón de confirmar: validaciones
        findViewById<Button>(R.id.btnConfirmarResolucion).setOnClickListener {
            val spEmpleado = findViewById<Spinner>(R.id.spEmpleado)
            val etSolucion = findViewById<EditText>(R.id.etSolucion)
            val solucion = etSolucion.text.toString().trim()
            val foto = archivoFoto

            if (spEmpleado.selectedItem == null) {
                Toast.makeText(this, "Seleccione quién resuelve", Toast.LENGTH_SHORT).show()
            } else if (solucion.isEmpty()) {
                etSolucion.error = "Describa la solución"
            } else if (foto == null || foto.length() == 0L) {
                Toast.makeText(this, "Debe tomar la fotografía final", Toast.LENGTH_SHORT).show()
            } else {
                // espera de API
                Toast.makeText(this, "Datos válidos. Falta conectar la API", Toast.LENGTH_SHORT).show()
            }
        }

        // si la pantalla se recreó (por ejemplo al girar), se recupera la foto
        val ruta = savedInstanceState?.getString("rutaFoto")
        if (ruta != null) {
            val archivo = File(ruta)
            archivoFoto = archivo
            uriFoto = FileProvider.getUriForFile(this, "$packageName.fileprovider", archivo)
            if (archivo.length() > 0) {
                mostrarFoto()
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("rutaFoto", archivoFoto?.absolutePath)
    }

    private fun mostrarFoto() {
        val ivFotoFinal = findViewById<ImageView>(R.id.ivFotoFinal)
        ivFotoFinal.setImageURI(uriFoto)
        ivFotoFinal.visibility = View.VISIBLE
    }
}