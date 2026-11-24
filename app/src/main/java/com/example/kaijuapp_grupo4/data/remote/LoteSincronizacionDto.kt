package com.example.kaijuapp_grupo4.data.remote

data class LoteSincronizacionRequest(
    val sucursal: String,
    val operaciones: List<OperacionDto>
)

data class OperacionDto(
    val accion: String,
    val productoId: String,
    val nombre: String? = null,
    val categoria: String? = null,
    val precio: Double? = null,
    val cantidadDisponible: Int? = null,
    val stockMinimo: Int? = null,
    val sucursal: String = "San Bernardo",
    val imagenUri: String? = null,
    val cantidad: Int? = null
)

data class LoteSincronizacionResponse(
    val sucursal: String = "",
    val operacionesRecibidas: Int = 0,
    val operacionesAplicadas: Int = 0,
    val ultimaSincronizacion: String = ""
)

data class EstadoSincronizacionSucursalResponse(
    val sucursal: String = "",
    val ultimaSincronizacion: String = "",
    val cambiosReales: Boolean = true,
    val estado: String = "DISPONIBLE",
    val textoUltimaSincronizacion: String = "Hace 15 minutos",
    val noDisponible: Boolean = false
)
