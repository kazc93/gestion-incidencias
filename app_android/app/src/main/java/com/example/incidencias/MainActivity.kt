package com.example.incidencias

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import com.example.incidencias.util.pantallaCompletaOscura
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.incidencias.data.model.Catalogo
import com.example.incidencias.data.model.IncidenciaListado
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.example.incidencias.data.repository.IncidenciaRepository
import com.example.incidencias.data.repository.Resultado
import com.example.incidencias.util.FechaUtils
import com.example.incidencias.util.aparecer
import com.example.incidencias.util.pulso
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private val repositorio = IncidenciaRepository()
    private lateinit var adapter: IncidenciaAdapter
    private lateinit var spEstado: Spinner
    private lateinit var spTipo: Spinner
    private lateinit var pbCargando: ProgressBar
    private lateinit var tvMensajeLista: TextView
    private lateinit var tvContador: TextView
    private lateinit var rvIncidencias: RecyclerView

    // para animar la entrada solo cuando la lista realmente cambia
    private var ultimaLista: List<IncidenciaListado>? = null

    // catálogos para los filtros (la posición 0 del spinner es "Todos")
    private var estados: List<Catalogo> = emptyList()
    private var tipos: List<Catalogo> = emptyList()
    private var filtroEstadoId: Int? = null
    private var filtroTipoId: Int? = null

    private var cargaJob: Job? = null

    // Android 17+ exige permiso para acceder a la red local (donde corre el backend)
    private val pedirPermisoRedLocal =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { concedido ->
            if (concedido) {
                cargarFiltros()
                cargarIncidencias()
            } else {
                mostrarEstadoLista("Sin permiso de red local no se puede conectar con el servidor")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pantallaCompletaOscura()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // lista de incidencias
        rvIncidencias = findViewById(R.id.rvIncidencias)
        rvIncidencias.layoutManager = LinearLayoutManager(this)
        adapter = IncidenciaAdapter(emptyList()) { incidencia ->
            val intent = Intent(this, DetalleActivity::class.java)
            intent.putExtra("id", incidencia.id)
            intent.putExtra("folio", incidencia.folio)
            intent.putExtra("titulo", incidencia.titulo)
            intent.putExtra("tipo", incidencia.tipo)
            intent.putExtra("prioridad", incidencia.prioridad)
            intent.putExtra("estado", incidencia.estadoClave)
            intent.putExtra("fecha", FechaUtils.formatearFecha(incidencia.fechaRegistro))
            startActivity(intent)
        }
        rvIncidencias.adapter = adapter
        pbCargando = findViewById(R.id.pbCargando)
        tvMensajeLista = findViewById(R.id.tvMensajeLista)
        tvContador = findViewById(R.id.tvContador)

        val btnAgregar = findViewById<ExtendedFloatingActionButton>(R.id.btnAgregar)
        btnAgregar.setOnClickListener {
            startActivity(Intent(this, RegistroActivity::class.java))
        }
        // el botón se encoge a solo "+" al bajar por la lista y se expande al subir
        rvIncidencias.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy > 6 && btnAgregar.isExtended) btnAgregar.shrink()
                else if (dy < -6 && !btnAgregar.isExtended) btnAgregar.extend()
            }
        })

        spEstado = findViewById(R.id.spFiltroEstado)
        spTipo = findViewById(R.id.spFiltroTipo)
        llenarSpinner(spEstado, "Estado: todos", emptyList())
        llenarSpinner(spTipo, "Tipo: todos", emptyList())
        spEstado.onItemSelectedListener = alSeleccionar { pos ->
            val nuevo = estados.getOrNull(pos - 1)?.id
            if (nuevo != filtroEstadoId) {
                filtroEstadoId = nuevo
                cargarIncidencias()
            }
        }
        spTipo.onItemSelectedListener = alSeleccionar { pos ->
            val nuevo = tipos.getOrNull(pos - 1)?.id
            if (nuevo != filtroTipoId) {
                filtroTipoId = nuevo
                cargarIncidencias()
            }
        }

        if (tienePermisoRedLocal()) {
            cargarFiltros()
        } else {
            pedirPermisoRedLocal.launch(PERMISO_RED_LOCAL)
        }
    }

    // se recarga al volver de registro/detalle para ver los cambios
    override fun onResume() {
        super.onResume()
        if (tienePermisoRedLocal()) cargarIncidencias()
    }

    private fun tienePermisoRedLocal(): Boolean =
        Build.VERSION.SDK_INT < 37 ||
            ContextCompat.checkSelfPermission(this, PERMISO_RED_LOCAL) == PackageManager.PERMISSION_GRANTED

    private fun cargarIncidencias() {
        cargaJob?.cancel()
        cargaJob = lifecycleScope.launch {
            pbCargando.visibility = View.VISIBLE
            tvMensajeLista.visibility = View.GONE

            // si la carga se cancela (cambio de filtro) no se llega aquí y la nueva carga controla el indicador
            val r = repositorio.obtenerIncidencias(filtroEstadoId, filtroTipoId)
            pbCargando.visibility = View.GONE

            when (r) {
                is Resultado.Exito -> {
                    if (r.data != ultimaLista) {
                        adapter.actualizar(r.data)
                        rvIncidencias.scheduleLayoutAnimation() // tarjetas en cascada
                        ultimaLista = r.data
                    }
                    actualizarContador(r.data.size)
                    if (r.data.isEmpty()) {
                        val hayFiltros = filtroEstadoId != null || filtroTipoId != null
                        mostrarEstadoLista(if (hayFiltros) "No hay incidencias con esos filtros" else "No hay incidencias registradas")
                    }
                }
                is Resultado.Error -> {
                    adapter.actualizar(emptyList())
                    ultimaLista = null
                    tvContador.visibility = View.INVISIBLE
                    mostrarEstadoLista(r.mensaje)
                }
            }
        }
    }

    /** texto centrado en la lista para vacío o error */
    private fun mostrarEstadoLista(texto: String) {
        tvMensajeLista.text = texto
        tvMensajeLista.aparecer()
    }

    private fun actualizarContador(total: Int) {
        val texto = if (total == 1) "1 incidencia" else "$total incidencias"
        if (tvContador.text.toString() != texto) {
            tvContador.text = texto
            tvContador.visibility = View.VISIBLE
            tvContador.pulso()
        }
    }

    private fun cargarFiltros() {
        lifecycleScope.launch {
            when (val r = repositorio.obtenerEstados()) {
                is Resultado.Exito -> {
                    estados = r.data
                    llenarSpinner(spEstado, "Estado: todos", estados)
                }
                is Resultado.Error -> mostrarMensaje(r.mensaje)
            }
            when (val r = repositorio.obtenerTipos()) {
                is Resultado.Exito -> {
                    tipos = r.data
                    llenarSpinner(spTipo, "Tipo: todos", tipos)
                }
                is Resultado.Error -> Unit // el error de conexión ya se mostró arriba
            }
        }
    }

    private fun llenarSpinner(spinner: Spinner, textoTodos: String, opciones: List<Catalogo>) {
        val nombres = listOf(textoTodos) + opciones.map { it.nombre }
        val spAdapter = ArrayAdapter(this, R.layout.item_spinner, nombres)
        spAdapter.setDropDownViewResource(R.layout.item_spinner_dropdown)
        spinner.adapter = spAdapter
    }

    private fun alSeleccionar(accion: (Int) -> Unit) = object : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) = accion(position)
        override fun onNothingSelected(parent: AdapterView<*>?) = Unit
    }

    private fun mostrarMensaje(texto: String) {
        Toast.makeText(this, texto, Toast.LENGTH_LONG).show()
    }

    companion object {
        private const val PERMISO_RED_LOCAL = "android.permission.ACCESS_LOCAL_NETWORK"
    }
}
