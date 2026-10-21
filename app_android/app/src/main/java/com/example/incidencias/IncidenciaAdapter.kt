package com.example.incidencias

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

class IncidenciaAdapter(private val lista: List<Incidencia>) :
    RecyclerView.Adapter<IncidenciaAdapter.IncidenciaViewHolder>() {

    // guarda las vistas de cada tarjeta
    class IncidenciaViewHolder(vista: View) : RecyclerView.ViewHolder(vista) {
        val tvListado: TextView = vista.findViewById(R.id.tvListado)
        val tvEstado: TextView = vista.findViewById(R.id.tvEstado)
        val tvTitulo: TextView = vista.findViewById(R.id.tvTitulo)
        val tvDetalle: TextView = vista.findViewById(R.id.tvDetalle)
        val tvFecha: TextView = vista.findViewById(R.id.tvFecha)
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
        holder.tvFecha.text = "Reportado el: ${incidencia.fecha}"
        holder.tvEstado.text = incidencia.estado.replace("_", " ")

        // color de la pastilla según el estado
        when (incidencia.estado) {
            "PENDIENTE" -> {
                holder.tvEstado.setBackgroundResource(R.drawable.bg_estado_pendiente)
                holder.tvEstado.setTextColor(ContextCompat.getColor(contexto, R.color.texto_pendiente))
            }
            "ASIGNADA" -> {
                holder.tvEstado.setBackgroundResource(R.drawable.bg_estado_asignada)
                holder.tvEstado.setTextColor(ContextCompat.getColor(contexto, R.color.texto_asignada))
            }
            "EN_ATENCION" -> {
                holder.tvEstado.setBackgroundResource(R.drawable.bg_estado_atencion)
                holder.tvEstado.setTextColor(ContextCompat.getColor(contexto, R.color.texto_en_atencion))
            }
            "RESUELTA" -> {
                holder.tvEstado.setBackgroundResource(R.drawable.bg_estado_resuelta)
                holder.tvEstado.setTextColor(ContextCompat.getColor(contexto, R.color.texto_resuelta))
            }
        }
    }
}