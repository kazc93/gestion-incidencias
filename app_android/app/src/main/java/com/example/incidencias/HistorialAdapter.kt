package com.example.incidencias

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.incidencias.data.model.Historial
import com.example.incidencias.util.FechaUtils

class HistorialAdapter(private var lista: List<Historial>) :
    RecyclerView.Adapter<HistorialAdapter.ViewHolder>() {

    class ViewHolder(vista: View) : RecyclerView.ViewHolder(vista) {
        val tvAccion: TextView = vista.findViewById(R.id.tvAccion)
        val tvCambioEstado: TextView = vista.findViewById(R.id.tvCambioEstado)
        val tvResponsableAccion: TextView = vista.findViewById(R.id.tvResponsableAccion)
        val tvFechaAccion: TextView = vista.findViewById(R.id.tvFechaAccion)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val vista = LayoutInflater.from(parent.context).inflate(R.layout.item_historial, parent, false)
        return ViewHolder(vista)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val h = lista[position]
        holder.tvAccion.text = h.accion.replace("_", " ")

        val destino = h.estadoDestino.replace("_", " ")
        holder.tvCambioEstado.text = h.estadoOrigen?.let { "${it.replace("_", " ")} → $destino" } ?: destino

        val comentario = h.comentario?.takeIf { it.isNotBlank() }?.let { "\n\"$it\"" } ?: ""
        holder.tvResponsableAccion.text = "Por: ${h.hechoPor}$comentario"
        holder.tvFechaAccion.text = FechaUtils.formatearFechaHora(h.fechaAccion)
    }

    override fun getItemCount() = lista.size

    fun actualizar(nueva: List<Historial>) {
        lista = nueva
        notifyDataSetChanged()
    }
}
