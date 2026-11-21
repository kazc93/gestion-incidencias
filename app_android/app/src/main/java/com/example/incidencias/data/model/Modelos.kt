package com.example.incidencias.data.model

// Nota: Gson no respeta la nulabilidad de Kotlin. Los campos que la API
// puede omitir o mandar en null se declaran como nullables.

/** Envoltura común de todas las respuestas: { success, message, data } */
data class ApiResponse<T>(
    val success: Boolean,
    val message: String?,
    val data: T?
)

/** tipos, prioridades, estados y empleados */
data class Catalogo(
    val id: Int,
    val clave: String,
    val nombre: String
)

data class IncidenciaListado(
    val id: Int,
    val folio: String,
    val titulo: String,
    val tipo: String,
    val prioridad: String,
    val estadoId: Int,
    val estadoClave: String,
    val estado: String,
    val fechaRegistro: String,
    val reporta: String,
    val responsable: String?
)

data class IncidenciaDetalle(
    val id: Int,
    val folio: String,
    val titulo: String,
    val tipo: String,
    val prioridad: String,
    val estadoId: Int,
    val estadoClave: String,
    val estado: String,
    val fechaRegistro: String,
    val reporta: String,
    val responsable: String?,
    val descripcion: String?,
    val responsableId: Int?,
    val solucion: String?,
    val fechaResolucion: String?,
    val fotos: List<FotoEvidencia>?,
    val historial: List<Historial>?
)

data class FotoEvidencia(
    val tipoEvidencia: String, // "INICIAL" / "FINAL"
    val url: String,
    val nombreArchivo: String?,
    val fechaCaptura: String?
)

data class Historial(
    val accion: String,
    val estadoOrigen: String?,
    val estadoDestino: String,
    val hechoPor: String,
    val comentario: String?,
    val fechaAccion: String
)

/** respuesta de POST api/incidencias */
data class RegistroRespuesta(
    val incidenciaId: Int,
    val folio: String,
    val estado: String
)

/** respuesta de asignar / iniciar / resolver */
data class CambioEstadoRespuesta(
    val incidenciaId: Int,
    val estado: String
)

data class AsignarRequest(
    val idResponsable: Int,
    val idAsigna: Int,
    val comentario: String?
)

data class IniciarRequest(
    val idEmpleado: Int
)

/** claves de estado que manda la API */
object EstadoClave {
    const val REGISTRADA = "REGISTRADA"
    // el documento del proyecto llama PENDIENTE al estado inicial; se aceptan ambas claves
    const val PENDIENTE = "PENDIENTE"
    const val ASIGNADA = "ASIGNADA"
    const val EN_ATENCION = "EN_ATENCION"
    const val RESUELTA = "RESUELTA"
}
