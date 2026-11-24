package com.example.kaijuapp_grupo4.sync

import com.example.kaijuapp_grupo4.data.OperacionPendienteDao
import com.example.kaijuapp_grupo4.data.remote.BackendApiService
import com.example.kaijuapp_grupo4.data.remote.LoteSincronizacionResponse
import com.example.kaijuapp_grupo4.model.OperacionPendiente
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Response

class SyncWorkerTest {

    private lateinit var operacionPendienteDao: OperacionPendienteDao
    private lateinit var backendApi: BackendApiService
    private lateinit var sincronizador: SincronizadorInventario

    @Before
    fun setUp() {
        operacionPendienteDao = mockk(relaxed = true)
        backendApi = mockk(relaxed = true)
        sincronizador = SincronizadorInventario(operacionPendienteDao, backendApi)
    }

    @Test
    fun testSincronizarSucursalSinPendientes() = runTest {
        coEvery { operacionPendienteDao.obtenerPorSucursal("San Bernardo") } returns emptyList()

        val resultado = sincronizador.sincronizarSucursal("San Bernardo")

        assertTrue(resultado is ResultadoSincronizacion.Exito)
        assertEquals(0, (resultado as ResultadoSincronizacion.Exito).operacionesAplicadas)
    }

    @Test
    fun testSincronizarSucursalConPendientesExitoso() = runTest {
        val pendientes = listOf(
            OperacionPendiente(id = 1L, accion = "ADD", productoId = "p1", sucursal = "San Bernardo")
        )
        coEvery { operacionPendienteDao.obtenerPorSucursal("San Bernardo") } returns pendientes
        coEvery { backendApi.enviarLoteSincronizacion(any()) } returns Response.success(
            LoteSincronizacionResponse(
                sucursal = "San Bernardo",
                operacionesRecibidas = 1,
                operacionesAplicadas = 1
            )
        )

        val resultado = sincronizador.sincronizarSucursal("San Bernardo")

        assertTrue(resultado is ResultadoSincronizacion.Exito)
        coVerify { operacionPendienteDao.eliminarPorIds(listOf(1L)) }
    }
}
