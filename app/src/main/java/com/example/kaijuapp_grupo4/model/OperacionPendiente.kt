package com.example.kaijuapp_grupo4.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "operaciones_pendientes")
data class OperacionPendiente(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val accion: String = "ADD", // ADD, UPDATE, SALE, DELETE, UPSERT
    val productoId: String = "",
    val nombre: String? = null,
    val categoria: String? = null,
    val precio: Double? = null,
    val cantidadDisponible: Int? = null,
    val stockMinimo: Int? = null,
    val sucursal: String = "San Bernardo",
    val imagenUri: String? = null,
    val cantidad: Int? = null,
    val fechaModificacion: Long = System.currentTimeMillis()
)
