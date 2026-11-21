package com.example.incidencias

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.incidencias.data.model.EstadoClave
import com.example.incidencias.data.model.IncidenciaListado
import com.example.incidencias.util.FechaUtils

class IncidenciaAdapter(
    private var lista: List<IncidenciaListado>,
    private val alTocar: (IncidenciaListado) -> Unit
) : RecyclerView.Adapter<IncidenciaAdapter.IncidenciaViewHolder>() {

    // guarda las vistas de cada tarjeta
    class IncidenciaViewHolder(vista: View) : RecyclerView.ViewHolder(vista) {
        val tvListado: TextView = vista.findViewById(R.id.tvListado)
        val tvEstado: TextView = vista.findViewById(R.id.tvEstado)
        val tvTitulo: TextView = vista.findViewById(R.id.tvTitulo)
        val tvDetalle: TextView = vista.findViewById(R.id.tvDetalle)
        val tvFecha: TextView = vista.findViewById(R.id.tvFecha)
        val vBarraEstado: View = vista.findViewById(R.id.vBarraEstado)
    }

    // reemplaza la lista con lo que llegó de la API
    @android.annotation.SuppressLint("NotifyDataSetChanged")
    fun actualizar(nuevaLista: List<IncidenciaListado>) {
        lista = nuevaLista
        notifyDataSetChanged()
    }

    // crea una tarjeta usando item_incidencia
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IncidenciaViewHolder {
        val vista = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_incidencia, parent, false)
        return IncidenciaViewHolder(vista)
    }

    // cantidad de tarjetas
    override fun getItemCount(): Int {
        return lista.size
    }

    override fun onBindViewHolder(holder: IncidenciaViewHolder, position: Int) {
        val incidencia = lista[position]
        val contexto = holder.itemView.context

        holder.tvListado.text = incidencia.folio
        holder.tvTitulo.text = incidencia.titulo
        holder.tvDetalle.text = "${incidencia.tipo} • Prioridad ${incidencia.prioridad}"
        holder.tvFecha.text = "Reportado el: ${FechaUtils.formatearFecha(incidencia.fechaRegistro)}"
        holder.tvEstado.text = incidencia.estadoClave.replace("_", " ")
        holder.itemView.setOnClickListener {
            alTocar(incidencia)
        }

        // color de la pastilla y de la barra lateral según el estado
        val (fondo, colorTexto) = when (incidencia.estadoClave) {
            EstadoClave.ASIGNADA -> R.drawable.bg_estado_asignada to R.color.texto_asignada
            EstadoClave.EN_ATENCION -> R.drawable.bg_estado_atencion to R.color.texto_en_atencion
            EstadoClave.RESUELTA -> R.drawable.bg_estado_resuelta to R.color.texto_resuelta
            else -> R.drawable.bg_estado_pendiente to R.color.texto_pendiente // REGISTRADA / PENDIENTE
        }
        val color = ContextCompat.getColor(contexto, colorTexto)
        holder.tvEstado.setBackgroundResource(fondo)
        holder.tvEstado.setTextColor(color)
        holder.vBarraEstado.backgroundTintList = ColorStateList.valueOf(color)
    }
}
