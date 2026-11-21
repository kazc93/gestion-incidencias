package com.example.incidencias.data.repository

import com.example.incidencias.data.model.ApiResponse
import com.example.incidencias.data.model.AsignarRequest
import com.example.incidencias.data.model.CambioEstadoRespuesta
import com.example.incidencias.data.model.Catalogo
import com.example.incidencias.data.model.IncidenciaDetalle
import com.example.incidencias.data.model.IncidenciaListado
import com.example.incidencias.data.model.IniciarRequest
import com.example.incidencias.data.model.RegistroRespuesta
import com.example.incidencias.data.remote.ApiService
import com.example.incidencias.data.remote.RetrofitClient
import com.google.gson.Gson
import kotlinx.coroutines.CancellationException
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Response
import java.io.File
import java.io.IOException

class IncidenciaRepository(
    private val api: ApiService = RetrofitClient.api
) {
    private val gson = Gson()

    // ---- catálogos ----
    suspend fun obtenerTipos() = ejecutar { api.obtenerTipos() }
    suspend fun obtenerPrioridades() = ejecutar { api.obtenerPrioridades() }
    suspend fun obtenerEstados() = ejecutar { api.obtenerEstados() }
    suspend fun obtenerEmpleados() = ejecutar { api.obtenerEmpleados() }

    // ---- incidencias ----
    suspend fun obtenerIncidencias(estadoId: Int? = null, tipoId: Int? = null): Resultado<List<IncidenciaListado>> =
        ejecutar { api.obtenerIncidencias(estadoId, tipoId) }

    suspend fun obtenerIncidencia(id: Int): Resultado<IncidenciaDetalle> =
        ejecutar { api.obtenerIncidencia(id) }

    /** fotoJpeg: archivo ya comprimido con ImagenUtils.prepararFotoJpeg */
    suspend fun registrarIncidencia(
        titulo: String,
        descripcion: String,
        idTipo: Int,
        idPrioridad: Int,
        idReporta: Int,
        fotoJpeg: File
    ): Resultado<RegistroRespuesta> = ejecutar {
        api.registrarIncidencia(
            titulo = titulo.aTexto(),
            descripcion = descripcion.aTexto(),
            idTipo = idTipo.toString().aTexto(),
            idPrioridad = idPrioridad.toString().aTexto(),
            idReporta = idReporta.toString().aTexto(),
            foto = fotoJpeg.aParteFoto()
        )
    }

    suspend fun asignarIncidencia(id: Int, idResponsable: Int, idAsigna: Int, comentario: String?): Resultado<CambioEstadoRespuesta> =
        ejecutar { api.asignarIncidencia(id, AsignarRequest(idResponsable, idAsigna, comentario?.takeIf { it.isNotBlank() })) }

    suspend fun iniciarIncidencia(id: Int, idEmpleado: Int): Resultado<CambioEstadoRespuesta> =
        ejecutar { api.iniciarIncidencia(id, IniciarRequest(idEmpleado)) }

    suspend fun resolverIncidencia(id: Int, idEmpleado: Int, solucion: String, fotoJpeg: File): Resultado<CambioEstadoRespuesta> =
        ejecutar {
            api.resolverIncidencia(
                id = id,
                idEmpleado = idEmpleado.toString().aTexto(),
                solucion = solucion.aTexto(),
                foto = fotoJpeg.aParteFoto()
            )
        }

    // ---- utilidades ----

    /**
     * Ejecuta la llamada y convierte la respuesta a Resultado:
     * - HTTP 2xx con success = true  -> Exito(data)
     * - success = false o HTTP 400/404/500 -> Error(message de la API)
     * - sin conexión -> Error con mensaje amigable
     */
    private suspend fun <T> ejecutar(llamada: suspend () -> Response<ApiResponse<T>>): Resultado<T> {
        return try {
            val respuesta = llamada()
            val cuerpo = respuesta.body()

            if (respuesta.isSuccessful && cuerpo != null) {
                val data = cuerpo.data
                when {
                    !cuerpo.success -> Resultado.Error(cuerpo.message ?: "Operación no realizada", respuesta.code())
                    data == null -> Resultado.Error(cuerpo.message ?: "La API no devolvió datos", respuesta.code())
                    else -> Resultado.Exito(data, cuerpo.message)
                }
            } else {
                Resultado.Error(mensajeDeError(respuesta), respuesta.code())
            }
        } catch (e: CancellationException) {
            throw e // no tragarse la cancelación de la corrutina
        } catch (e: IOException) {
            Resultado.Error("No se pudo conectar con el servidor. Verifica que la API esté encendida y la URL sea correcta.")
        } catch (e: Exception) {
            Resultado.Error("Error inesperado: ${e.message}")
        }
    }

    /** los errores 400/404/500 traen { success, message, data } en el body */
    private fun mensajeDeError(respuesta: Response<*>): String {
        val texto = try {
            respuesta.errorBody()?.string()
        } catch (e: IOException) {
            null
        }
        val mensaje = try {
            texto?.let { gson.fromJson(it, ApiResponse::class.java)?.message }
        } catch (e: Exception) {
            null
        }
        return mensaje?.takeIf { it.isNotBlank() } ?: "Error del servidor (${respuesta.code()})"
    }

    private fun String.aTexto(): RequestBody = toRequestBody("text/plain".toMediaType())

    private fun File.aParteFoto(): MultipartBody.Part =
        MultipartBody.Part.createFormData("foto", name, asRequestBody("image/jpeg".toMediaType()))
}
