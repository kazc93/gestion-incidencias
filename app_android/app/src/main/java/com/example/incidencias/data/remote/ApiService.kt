package com.example.incidencias.data.remote

import com.example.incidencias.data.model.ApiResponse
import com.example.incidencias.data.model.AsignarRequest
import com.example.incidencias.data.model.CambioEstadoRespuesta
import com.example.incidencias.data.model.Catalogo
import com.example.incidencias.data.model.IncidenciaDetalle
import com.example.incidencias.data.model.IncidenciaListado
import com.example.incidencias.data.model.IniciarRequest
import com.example.incidencias.data.model.RegistroRespuesta
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    // ---- catálogos ----
    @GET("api/tipos")
    suspend fun obtenerTipos(): Response<ApiResponse<List<Catalogo>>>

    @GET("api/prioridades")
    suspend fun obtenerPrioridades(): Response<ApiResponse<List<Catalogo>>>

    @GET("api/estados")
    suspend fun obtenerEstados(): Response<ApiResponse<List<Catalogo>>>

    @GET("api/empleados")
    suspend fun obtenerEmpleados(): Response<ApiResponse<List<Catalogo>>>

    // ---- incidencias ----
    // Retrofit omite el parámetro si es null, así los filtros son opcionales
    @GET("api/incidencias")
    suspend fun obtenerIncidencias(
        @Query("estadoId") estadoId: Int? = null,
        @Query("tipoId") tipoId: Int? = null
    ): Response<ApiResponse<List<IncidenciaListado>>>

    @GET("api/incidencias/{id}")
    suspend fun obtenerIncidencia(@Path("id") id: Int): Response<ApiResponse<IncidenciaDetalle>>

    @Multipart
    @POST("api/incidencias")
    suspend fun registrarIncidencia(
        @Part("titulo") titulo: RequestBody,
        @Part("descripcion") descripcion: RequestBody,
        @Part("idTipo") idTipo: RequestBody,
        @Part("idPrioridad") idPrioridad: RequestBody,
        @Part("idReporta") idReporta: RequestBody,
        @Part foto: MultipartBody.Part
    ): Response<ApiResponse<RegistroRespuesta>>

    @POST("api/incidencias/{id}/asignar")
    suspend fun asignarIncidencia(
        @Path("id") id: Int,
        @Body body: AsignarRequest
    ): Response<ApiResponse<CambioEstadoRespuesta>>

    @POST("api/incidencias/{id}/iniciar")
    suspend fun iniciarIncidencia(
        @Path("id") id: Int,
        @Body body: IniciarRequest
    ): Response<ApiResponse<CambioEstadoRespuesta>>

    @Multipart
    @POST("api/incidencias/{id}/resolver")
    suspend fun resolverIncidencia(
        @Path("id") id: Int,
        @Part("idEmpleado") idEmpleado: RequestBody,
        @Part("solucion") solucion: RequestBody,
        @Part foto: MultipartBody.Part
    ): Response<ApiResponse<CambioEstadoRespuesta>>
}
