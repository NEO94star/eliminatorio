package com.example.kaijuapp_grupo4.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.kaijuapp_grupo4.model.Movimiento
import kotlinx.coroutines.flow.Flow

@Dao
interface MovimientoDao {

    @Insert
    suspend fun insertar(movimiento: Movimiento)

    @Query("SELECT * FROM movimientos ORDER BY fecha DESC")
    fun observarTodos(): Flow<List<Movimiento>>

    @Query("SELECT COALESCE(SUM(cantidad), 0) FROM movimientos WHERE tipo = :tipo")
    fun contarPorTipo(tipo: String): Flow<Int>
}
