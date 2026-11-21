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
import com.example.incidencias.util.pantallaCompletaOscura
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.incidencias.data.model.Catalogo
import com.example.incidencias.data.remote.ApiConfig
import com.example.incidencias.data.repository.IncidenciaRepository
import com.example.incidencias.data.repository.Resultado
import com.example.incidencias.util.ImagenUtils
import com.example.incidencias.util.aparecer
import com.example.incidencias.util.sacudir
import com.example.incidencias.util.idSeleccionado
import com.example.incidencias.util.llenarCatalogo
import com.example.incidencias.util.seleccionarId
import kotlinx.coroutines.launch
import java.io.File

class ResolverActivity : AppCompatActivity() {

    private val repositorio = IncidenciaRepository()
    private var idIncidencia = 0
    private var empleados: List<Catalogo> = emptyList()

    private var uriFoto: Uri? = null
    private var archivoFoto: File? = null

    private lateinit var spEmpleado: Spinner
    private lateinit var etSolucion: EditText
    private lateinit var btnConfirmar: Button

    private val camara = registerForActivityResult(ActivityResultContracts.TakePicture()) { exito ->
        if (exito) mostrarFoto()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pantallaCompletaOscura()
        setContentView(R.layout.activity_resolver)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        idIncidencia = intent.getIntExtra("id", 0)
        findViewById<TextView>(R.id.tvFolio).text = intent.getStringExtra("folio") ?: ""
        findViewById<TextView>(R.id.tvTitulo).text = intent.getStringExtra("titulo") ?: ""

        spEmpleado = findViewById(R.id.spEmpleado)
        etSolucion = findViewById(R.id.etSolucion)
        btnConfirmar = findViewById(R.id.btnConfirmarResolucion)
        spEmpleado.llenarCatalogo("Cargando...", emptyList())
        cargarEmpleados(savedInstanceState)

        findViewById<View>(R.id.btnVolver).setOnClickListener { finish() }
        findViewById<Button>(R.id.btnTomarFotoFinal).setOnClickListener { tomarFoto() }
        btnConfirmar.setOnClickListener { confirmar() }

        // restaurar la foto si se giró la pantalla o el sistema cerró la app al abrir la cámara
        savedInstanceState?.getString("rutaFoto")?.let { ruta ->
            val archivo = File(ruta)
            archivoFoto = archivo
            uriFoto = FileProvider.getUriForFile(this, "$packageName.fileprovider", archivo)
            if (archivo.length() > 0) mostrarFoto()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("rutaFoto", archivoFoto?.absolutePath)
        spEmpleado.idSeleccionado(empleados)?.let { outState.putInt("idEmpleado", it) }
    }

    private fun cargarEmpleados(estado: Bundle?) {
        lifecycleScope.launch {
            when (val r = repositorio.obtenerEmpleados()) {
                is Resultado.Exito -> empleados = r.data
                is Resultado.Error -> mostrarMensaje(r.mensaje)
            }
            spEmpleado.llenarCatalogo("Selecciona empleado", empleados)
            val idInicial = when {
                estado?.containsKey("idEmpleado") == true -> estado.getInt("idEmpleado")
                intent.hasExtra("responsableId") -> intent.getIntExtra("responsableId", 0)
                else -> null
            }
            spEmpleado.seleccionarId(empleados, idInicial)
        }
    }

    private fun tomarFoto() {
        val carpeta = File(filesDir, "fotos")
        if (!carpeta.exists()) carpeta.mkdirs()
        val archivo = File(carpeta, "final_${System.currentTimeMillis()}.jpg")
        archivoFoto = archivo
        val uri = FileProvider.getUriForFile(this, "$packageName.fileprovider", archivo)
        uriFoto = uri
        camara.launch(uri)
    }

    private fun mostrarFoto() {
        val ivFoto = findViewById<ImageView>(R.id.ivFotoFinal)
        ivFoto.setImageURI(null) // fuerza recargar si se repite la foto con otro archivo
        ivFoto.setImageURI(uriFoto)
        ivFoto.aparecer()
        findViewById<Button>(R.id.btnTomarFotoFinal).text = "Tomar otra fotografía"
    }

    private fun confirmar() {
        val idEmpleado = spEmpleado.idSeleccionado(empleados)
        val solucion = etSolucion.text.toString().trim()
        val uri = uriFoto
        val hayFoto = uri != null && (archivoFoto?.length() ?: 0L) > 0

        when {
            idIncidencia == 0 -> { mostrarMensaje("No se recibió la incidencia"); return }
            idEmpleado == null -> { spEmpleado.sacudir(); mostrarMensaje("Selecciona quién resolvió"); return }
            solucion.isEmpty() -> { etSolucion.error = "Describe la solución"; etSolucion.requestFocus(); etSolucion.sacudir(); return }
            !hayFoto -> { findViewById<View>(R.id.btnTomarFotoFinal).sacudir(); mostrarMensaje("Toma la fotografía final"); return }
        }

        lifecycleScope.launch {
            ponerEnviando(true)
            var fotoJpeg: File? = null
            try {
                fotoJpeg = try {
                    ImagenUtils.prepararFotoJpeg(this@ResolverActivity, uri, ApiConfig.MAX_FOTO_RESOLVER_BYTES)
                } catch (e: IllegalArgumentException) {
                    mostrarMensaje(e.message ?: "No se pudo procesar la fotografía")
                    return@launch
                }

                when (val r = repositorio.resolverIncidencia(idIncidencia, idEmpleado, solucion, fotoJpeg)) {
                    is Resultado.Exito -> {
                        mostrarMensaje(r.mensaje ?: "Incidencia resuelta")
                        finish()
                    }
                    is Resultado.Error -> mostrarMensaje(r.mensaje)
                }
            } finally {
                fotoJpeg?.delete()
                ponerEnviando(false)
            }
        }
    }

    private fun ponerEnviando(enviando: Boolean) {
        btnConfirmar.isEnabled = !enviando
        btnConfirmar.text = if (enviando) "RESOLVIENDO..." else "RESOLVER INCIDENCIA"
    }

    private fun mostrarMensaje(texto: String) {
        Toast.makeText(this, texto, Toast.LENGTH_LONG).show()
    }
}
