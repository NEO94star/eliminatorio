package com.example.kaijuapp_grupo4.sync

import com.example.kaijuapp_grupo4.data.OperacionPendienteDao
import com.example.kaijuapp_grupo4.data.remote.BackendApiService
import com.example.kaijuapp_grupo4.data.remote.LoteSincronizacionRequest
import com.example.kaijuapp_grupo4.data.remote.OperacionDto
import com.example.kaijuapp_grupo4.data.remote.RetrofitClient

class SincronizadorInventario(
    private val operacionPendienteDao: OperacionPendienteDao,
    private val backendApi: BackendApiService = RetrofitClient.backendService
) {

    suspend fun sincronizarSucursal(sucursal: String): ResultadoSincronizacion {
        val pendientes = operacionPendienteDao.obtenerPorSucursal(sucursal)
        if (pendientes.isEmpty()) {
            return ResultadoSincronizacion.Exito(
                sucursal = sucursal,
                operacionesAplicadas = 0,
                mensaje = "No hay operaciones pendientes para sincronizar"
            )
        }

        val operacionesDto = pendientes.map { op ->
            OperacionDto(
                accion = op.accion,
                productoId = op.productoId,
                nombre = op.nombre,
                categoria = op.categoria,
                precio = op.precio,
                cantidadDisponible = op.cantidadDisponible,
                stockMinimo = op.stockMinimo,
                sucursal = op.sucursal,
                imagenUri = op.imagenUri,
                cantidad = op.cantidad
            )
        }

        val request = LoteSincronizacionRequest(
            sucursal = sucursal,
            operaciones = operacionesDto
        )

        return try {
            val response = backendApi.enviarLoteSincronizacion(request)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                val aplicadas = body.operacionesAplicadas

                if (aplicadas > 0) {
                    val idsAEliminar = pendientes.take(aplicadas).map { it.id }
                    operacionPendienteDao.eliminarPorIds(idsAEliminar)
                }

                ResultadoSincronizacion.Exito(
                    sucursal = sucursal,
                    operacionesAplicadas = aplicadas,
                    mensaje = "Sincronización exitosa ($aplicadas/${pendientes.size} aplicadas)"
                )
            } else {
                ResultadoSincronizacion.Error(
                    sucursal = sucursal,
                    mensaje = "Error del servidor: ${response.code()}"
                )
            }
        } catch (e: Exception) {
            ResultadoSincronizacion.Error(
                sucursal = sucursal,
                mensaje = "Sin conexión al servidor: ${e.localizedMessage ?: "Error de red"}"
            )
        }
    }
}

sealed class ResultadoSincronizacion {
    data class Exito(val sucursal: String, val operacionesAplicadas: Int, val mensaje: String) : ResultadoSincronizacion()
    data class Error(val sucursal: String, val mensaje: String) : ResultadoSincronizacion()
}
