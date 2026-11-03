package com.example.incidencias

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.io.File
import android.widget.EditText
import android.widget.Toast

class RegistroActivity : AppCompatActivity() {
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
        setContentView(R.layout.activity_registro)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        findViewById<Button>(R.id.btnTomarFoto).setOnClickListener {
            val carpeta = File(filesDir, "fotos")
            if (!carpeta.exists()) {
                carpeta.mkdirs()
            }
            val archivo = File(carpeta, "${System.currentTimeMillis()}.jpg")
            archivoFoto = archivo
            val uri = FileProvider.getUriForFile(this, "$packageName.fileprovider", archivo)
            uriFoto = uri
            camara.launch(uri)
        }
        findViewById<Button>(R.id.btnGuardar).setOnClickListener {
            val etTitulo = findViewById<EditText>(R.id.etTitulo)
            val etDescripcion = findViewById<EditText>(R.id.etDescripcion)
            val titulo = etTitulo.text.toString().trim()
            val desripcion = etDescripcion.text.toString().trim()
            val foto = archivoFoto

            if (titulo.length < 5){
                etTitulo.error = "El título debe tener al menos 5 caracteres"
            }else if (desripcion.isEmpty()){
                etDescripcion.error = "La descripción es obligatoria"
            } else if (foto == null || foto.length() == 0L) {
                Toast.makeText(this, "Debe tomar la fotografía inicial", Toast.LENGTH_SHORT).show()
            } else {
                //espera de API
                Toast.makeText(this, "Datos válidos. Falta conectar la API", Toast.LENGTH_SHORT).show()
            }
        }

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
        val ivFoto = findViewById<ImageView>(R.id.ivFoto)
        ivFoto.setImageURI(uriFoto)
        ivFoto.visibility = View.VISIBLE
    }
}