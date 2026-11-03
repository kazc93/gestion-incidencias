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