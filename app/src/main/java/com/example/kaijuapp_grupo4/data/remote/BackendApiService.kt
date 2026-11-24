package com.example.kaijuapp_grupo4.data.remote

import com.example.kaijuapp_grupo4.model.Producto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface BackendApiService {

    @POST("api/sincronizacion/lote")
    suspend fun enviarLoteSincronizacion(
        @Body request: LoteSincronizacionRequest
    ): Response<LoteSincronizacionResponse>

    @GET("api/sincronizacion/{sucursal}")
    suspend fun obtenerEstadoSincronizacion(
        @Path("sucursal") sucursal: String
    ): Response<EstadoSincronizacionSucursalResponse>

    @GET("api/sucursales")
    suspend fun obtenerSucursales(): List<String>

    @GET("api/productos")
    suspend fun obtenerProductosPorSucursal(
        @Query("sucursal") sucursal: String
    ): Response<List<Producto>>

    @GET("api/sucursales/{sucursal}/stock-bajo")
    suspend fun obtenerStockBajo(
        @Path("sucursal") sucursal: String
    ): List<Producto>

    @GET("api/productos")
    suspend fun obtenerTodosProductos(): List<Producto>

    @GET("api/productos/{id}")
    suspend fun obtenerProductoPorId(@Path("id") id: String): Response<Producto>

    @POST("api/productos")
    suspend fun crearProducto(@Body producto: Producto): Response<Producto>

    @PUT("api/productos/{id}")
    suspend fun actualizarProducto(
        @Path("id") id: String,
        @Body producto: Producto
    ): Response<Producto>

    @DELETE("api/productos/{id}")
    suspend fun eliminarProducto(@Path("id") id: String): Response<Unit>
}
