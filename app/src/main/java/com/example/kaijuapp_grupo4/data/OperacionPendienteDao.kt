package com.example.kaijuapp_grupo4.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.kaijuapp_grupo4.model.OperacionPendiente
import kotlinx.coroutines.flow.Flow

@Dao
interface OperacionPendienteDao {

    @Query("SELECT * FROM operaciones_pendientes ORDER BY id ASC")
    fun observarTodas(): Flow<List<OperacionPendiente>>

    @Query("SELECT * FROM operaciones_pendientes ORDER BY id ASC")
    suspend fun obtenerTodas(): List<OperacionPendiente>

    @Query("SELECT * FROM operaciones_pendientes WHERE sucursal = :sucursal ORDER BY id ASC")
    suspend fun obtenerPorSucursal(sucursal: String): List<OperacionPendiente>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(operacion: OperacionPendiente): Long

    @Query("DELETE FROM operaciones_pendientes WHERE id IN (:ids)")
    suspend fun eliminarPorIds(ids: List<Long>)

    @Query("DELETE FROM operaciones_pendientes WHERE sucursal = :sucursal")
    suspend fun eliminarPorSucursal(sucursal: String)
}
