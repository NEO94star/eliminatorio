package com.example.kaijuapp_grupo4.repository

import com.example.kaijuapp_grupo4.data.MovimientoDao
import com.example.kaijuapp_grupo4.data.OperacionPendienteDao
import com.example.kaijuapp_grupo4.data.ProductoDao
import com.example.kaijuapp_grupo4.data.remote.BackendApiService
import com.example.kaijuapp_grupo4.data.remote.EstadoSincronizacionSucursalResponse
import com.example.kaijuapp_grupo4.data.remote.ExchangeRateApiService
import com.example.kaijuapp_grupo4.data.remote.RetrofitClient
import com.example.kaijuapp_grupo4.model.Movimiento
import com.example.kaijuapp_grupo4.model.OperacionPendiente
import com.example.kaijuapp_grupo4.model.Producto
import com.example.kaijuapp_grupo4.sync.ResultadoSincronizacion
import com.example.kaijuapp_grupo4.sync.SincronizadorInventario
import kotlinx.coroutines.flow.Flow
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class InventarioRepository(
    private val productoDao: ProductoDao,
    private val movimientoDao: MovimientoDao,
    private val operacionPendienteDao: OperacionPendienteDao? = null,
    private val backendApi: BackendApiService = RetrofitClient.backendService,
    private val exchangeRateApi: ExchangeRateApiService = RetrofitClient.exchangeRateService
) {
    val productos: Flow<List<Producto>> = productoDao.observarTodos()

    fun observarProductosPorSucursal(sucursal: String): Flow<List<Producto>> {
        return productoDao.observarPorSucursal(sucursal)
    }

    suspend fun agregarProducto(producto: Producto) {
        // 1. Guardar primero e inmediatamente en Room (Offline-First)
        productoDao.insertar(producto)
        movimientoDao.insertar(crearMovimiento(producto.id, "ENTRADA", producto.cantidadDisponible))

        // 2. Encolar operación pendiente de sincronización (ADD)
        operacionPendienteDao?.insertar(
            OperacionPendiente(
                accion = "ADD",
                productoId = producto.id,
                nombre = producto.nombre,
                categoria = producto.categoria,
                precio = producto.precio,
                cantidadDisponible = producto.cantidadDisponible,
                stockMinimo = producto.stockMinimo,
                sucursal = producto.sucursal,
                imagenUri = producto.imagenUri
            )
        )

        // 3. Intentar sincronización con backend en EC2
        sincronizarConBackend(producto.sucursal)
    }

    suspend fun registrarVenta(productoId: String, cantidad: Int): Boolean {
        val producto = productoDao.obtenerPorId(productoId) ?: return false
        val filasModificadas = productoDao.descontarStock(productoId, cantidad)
        if (filasModificadas == 0) return false

        movimientoDao.insertar(crearMovimiento(productoId, "VENTA", cantidad))

        // Encolar operación pendiente SALE
        operacionPendienteDao?.insertar(
            OperacionPendiente(
                accion = "SALE",
                productoId = productoId,
                sucursal = producto.sucursal,
                cantidad = cantidad
            )
        )

        sincronizarConBackend(producto.sucursal)
        return true
    }

    // Sincronización bidireccional real
    suspend fun sincronizarConBackend(sucursal: String): Boolean {
        val resultadoEnvio = enviarPendientesAlBackend(sucursal)
        if (resultadoEnvio is ResultadoSincronizacion.Error) return false
        return actualizarCatalogoDesdeBackend(sucursal) is ResultadoActualizacionCatalogo.Exito
    }

    suspend fun enviarPendientesAlBackend(sucursal: String): ResultadoSincronizacion {
        val dao = operacionPendienteDao ?: return ResultadoSincronizacion.Error(
            sucursal = sucursal,
            mensaje = "No está disponible la cola de operaciones pendientes"
        )
        return SincronizadorInventario(dao, backendApi).sincronizarSucursal(sucursal)
    }

    suspend fun actualizarCatalogoDesdeBackend(
        sucursal: String
    ): ResultadoActualizacionCatalogo {
        return try {
            val response = backendApi.obtenerProductosPorSucursal(sucursal)
            if (!response.isSuccessful) {
                return ResultadoActualizacionCatalogo.Error(
                    "El backend respondió con HTTP ${response.code()}"
                )
            }

            val productosRemotos = response.body()
                ?: return ResultadoActualizacionCatalogo.Error(
                    "El backend respondió sin un catálogo de productos"
                )

            productosRemotos.forEach { producto ->
                productoDao.insertar(producto.copy(sucursal = sucursal))
            }
            ResultadoActualizacionCatalogo.Exito(productosRemotos.size)
        } catch (e: IOException) {
            ResultadoActualizacionCatalogo.Error(
                "No se pudo conectar con el backend: ${e.localizedMessage ?: "error de red"}"
            )
        }
    }

    // Consultar estado real de sincronización de la sucursal en EC2
    suspend fun obtenerEstadoSincronizacionBackend(sucursal: String): EstadoSincronizacionSucursalResponse? {
        return try {
            val response = backendApi.obtenerEstadoSincronizacion(sucursal)
            if (response.isSuccessful) response.body() else null
        } catch (_: Exception) {
            null
        }
    }

    suspend fun obtenerTasaUsdEur(): Pair<Double, Double>? {
        return try {
            val respuesta = exchangeRateApi.obtenerTasas("USD")
            val eurRate = respuesta.rates["EUR"] ?: 0.92
            Pair(1.0, eurRate)
        } catch (_: Exception) {
            null
        }
    }

    sealed class ResultadoActualizacionCatalogo {
        data class Exito(val productosActualizados: Int) : ResultadoActualizacionCatalogo()
        data class Error(val mensaje: String) : ResultadoActualizacionCatalogo()
    }

    private fun crearMovimiento(productoId: String, tipo: String, cantidad: Int): Movimiento {
        val fecha = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.forLanguageTag("es-CL")).format(Date())
        return Movimiento(
            id = UUID.randomUUID().toString(),
            productoId = productoId,
            tipo = tipo,
            cantidad = cantidad,
            fecha = fecha
        )
    }
}
