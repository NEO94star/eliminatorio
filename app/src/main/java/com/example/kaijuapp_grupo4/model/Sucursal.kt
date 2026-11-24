package com.example.kaijuapp_grupo4.model

data class Sucursal(
    val id: String,
    val nombre: String,
    val direccion: String,
    val diasSinCambios: Int = 0,
    val tiempoTranscurridoFormateado: String = "Hace 15 minutos",
    val cambioLlegados: Int = 0,
    val cambioVendidos: Int = 0,
    val cambioMerma: Int = 0,
    val productos: List<Producto> = emptyList()
) {
    val estaDisponible: Boolean
        get() = diasSinCambios <= 31
}
