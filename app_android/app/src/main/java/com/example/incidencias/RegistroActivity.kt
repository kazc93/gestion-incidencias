package com.example.incidencias

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
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

class RegistroActivity : AppCompatActivity() {
    private var uriFoto: Uri? = null
    private var archivoFoto: File? = null

    private val repositorio = IncidenciaRepository()
    private lateinit var spReporta: Spinner
    private lateinit var spTipo: Spinner
    private lateinit var spPrioridad: Spinner
    private lateinit var etTitulo: EditText
    private lateinit var etDescripcion: EditText
    private lateinit var btnGuardar: Button

    // catálogos de la API (la posición 0 de cada spinner es el texto "Selecciona...")
    private var empleados: List<Catalogo> = emptyList()
    private var tipos: List<Catalogo> = emptyList()
    private var prioridades: List<Catalogo> = emptyList()

    private val camara = registerForActivityResult(ActivityResultContracts.TakePicture()) { exito ->
        if (exito) {
            mostrarFoto()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pantallaCompletaOscura()
        setContentView(R.layout.activity_registro)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        spReporta = findViewById(R.id.spReporta)
        spTipo = findViewById(R.id.spTipo)
        spPrioridad = findViewById(R.id.spPrioridad)
        spReporta.llenarCatalogo("Cargando...", emptyList())
        spTipo.llenarCatalogo("Cargando...", emptyList())
        spPrioridad.llenarCatalogo("Cargando...", emptyList())
        cargarCatalogos(savedInstanceState)

        etTitulo = findViewById(R.id.etTitulo)
        etDescripcion = findViewById(R.id.etDescripcion)
        btnGuardar = findViewById(R.id.btnGuardar)
        btnGuardar.setOnClickListener { guardar() }

        findViewById<View>(R.id.btnVolver).setOnClickListener { finish() }

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
        // los catálogos se recargan de la API, así que se guarda la selección por id
        spReporta.idSeleccionado(empleados)?.let { outState.putInt("idReporta", it) }
        spTipo.idSeleccionado(tipos)?.let { outState.putInt("idTipo", it) }
        spPrioridad.idSeleccionado(prioridades)?.let { outState.putInt("idPrioridad", it) }
    }

    private fun guardar() {
        val titulo = etTitulo.text.toString().trim()
        val descripcion = etDescripcion.text.toString().trim()
        val idReporta = spReporta.idSeleccionado(empleados)
        val idTipo = spTipo.idSeleccionado(tipos)
        val idPrioridad = spPrioridad.idSeleccionado(prioridades)
        val uri = uriFoto
        // si se canceló la cámara el archivo existe pero está vacío
        val hayFoto = uri != null && (archivoFoto?.length() ?: 0L) > 0

        when {
            titulo.isEmpty() -> { etTitulo.error = "Escribe un título"; etTitulo.requestFocus(); etTitulo.sacudir(); return }
            descripcion.isEmpty() -> { etDescripcion.error = "Escribe una descripción"; etDescripcion.requestFocus(); etDescripcion.sacudir(); return }
            idReporta == null -> { spReporta.sacudir(); mostrarMensaje("Selecciona quién reporta"); return }
            idTipo == null -> { spTipo.sacudir(); mostrarMensaje("Selecciona el tipo"); return }
            idPrioridad == null -> { spPrioridad.sacudir(); mostrarMensaje("Selecciona la prioridad"); return }
            !hayFoto -> { findViewById<View>(R.id.btnTomarFoto).sacudir(); mostrarMensaje("Toma una fotografía de la incidencia"); return }
        }

        lifecycleScope.launch {
            ponerGuardando(true)
            var fotoJpeg: File? = null
            try {
                fotoJpeg = try {
                    ImagenUtils.prepararFotoJpeg(this@RegistroActivity, uri, ApiConfig.MAX_FOTO_REGISTRO_BYTES)
                } catch (e: IllegalArgumentException) {
                    mostrarMensaje(e.message ?: "No se pudo procesar la fotografía")
                    return@launch
                }

                when (val r = repositorio.registrarIncidencia(titulo, descripcion, idTipo, idPrioridad, idReporta, fotoJpeg)) {
                    is Resultado.Exito -> {
                        mostrarMensaje("Incidencia ${r.data.folio} registrada")
                        // MainActivity recarga la lista en onResume
                        finish()
                    }
                    is Resultado.Error -> mostrarMensaje(r.mensaje)
                }
            } finally {
                fotoJpeg?.delete() // temporal en cacheDir
                ponerGuardando(false)
            }
        }
    }

    /** evita doble envío mientras se sube la foto */
    private fun ponerGuardando(guardando: Boolean) {
        btnGuardar.isEnabled = !guardando
        btnGuardar.text = if (guardando) "GUARDANDO..." else "GUARDAR INCIDENCIA"
    }

    private fun cargarCatalogos(estadoGuardado: Bundle?) {
        lifecycleScope.launch {
            var huboError = false

            when (val r = repositorio.obtenerEmpleados()) {
                is Resultado.Exito -> empleados = r.data
                is Resultado.Error -> { huboError = true; mostrarMensaje(r.mensaje) }
            }
            spReporta.llenarCatalogo("Selecciona quién reporta", empleados)
            spReporta.seleccionarId(empleados, estadoGuardado.idGuardado("idReporta"))

            when (val r = repositorio.obtenerTipos()) {
                is Resultado.Exito -> tipos = r.data
                is Resultado.Error -> if (!huboError) { huboError = true; mostrarMensaje(r.mensaje) }
            }
            spTipo.llenarCatalogo("Elegir", tipos)
            spTipo.seleccionarId(tipos, estadoGuardado.idGuardado("idTipo"))

            when (val r = repositorio.obtenerPrioridades()) {
                is Resultado.Exito -> prioridades = r.data
                is Resultado.Error -> if (!huboError) mostrarMensaje(r.mensaje)
            }
            spPrioridad.llenarCatalogo("Elegir", prioridades)
            spPrioridad.seleccionarId(prioridades, estadoGuardado.idGuardado("idPrioridad"))
        }
    }

    private fun Bundle?.idGuardado(clave: String): Int? =
        if (this != null && containsKey(clave)) getInt(clave) else null

    private fun mostrarMensaje(texto: String) {
        Toast.makeText(this, texto, Toast.LENGTH_LONG).show()
    }

    private fun mostrarFoto() {
        val ivFoto = findViewById<ImageView>(R.id.ivFoto)
        ivFoto.setImageURI(null) // fuerza recargar si se repite la foto
        ivFoto.setImageURI(uriFoto)
        ivFoto.aparecer()
        findViewById<Button>(R.id.btnTomarFoto).text = "Tomar otra fotografía"
    }
}