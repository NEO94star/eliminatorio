package com.example.kaijuapp_grupo4.repository

import com.example.kaijuapp_grupo4.data.MovimientoDao
import com.example.kaijuapp_grupo4.data.ProductoDao
import com.example.kaijuapp_grupo4.data.remote.BackendApiService
import com.example.kaijuapp_grupo4.model.Producto
import com.example.kaijuapp_grupo4.repository.InventarioRepository.ResultadoActualizacionCatalogo
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response

class InventarioRepositoryTest {

    @Test
    fun actualizarCatalogoDesdeBackendGuardaLosProductosEnRoom() = runTest {
        val productoDao = mockk<ProductoDao>(relaxed = true)
        val backendApi = mockk<BackendApiService>()
        val producto = Producto(
            id = "p-1",
            nombre = "Figura Kaiju",
            sucursal = "Maipú"
        )
        val repository = InventarioRepository(
            productoDao = productoDao,
            movimientoDao = mockk<MovimientoDao>(relaxed = true),
            backendApi = backendApi
        )
        coEvery {
            backendApi.obtenerProductosPorSucursal("Maipú")
        } returns Response.success(listOf(producto))

        val resultado = repository.actualizarCatalogoDesdeBackend("Maipú")

        assertTrue(resultado is ResultadoActualizacionCatalogo.Exito)
        assertEquals(
            1,
            (resultado as ResultadoActualizacionCatalogo.Exito).productosActualizados
        )
        coVerify { productoDao.insertar(producto) }
    }

    @Test
    fun actualizarCatalogoDesdeBackendMuestraErroresHttp() = runTest {
        val backendApi = mockk<BackendApiService>()
        val repository = InventarioRepository(
            productoDao = mockk<ProductoDao>(relaxed = true),
            movimientoDao = mockk<MovimientoDao>(relaxed = true),
            backendApi = backendApi
        )
        coEvery {
            backendApi.obtenerProductosPorSucursal("Maipú")
        } returns Response.error<List<Producto>>(503, byteArrayOf().toResponseBody())

        val resultado = repository.actualizarCatalogoDesdeBackend("Maipú")

        assertTrue(resultado is ResultadoActualizacionCatalogo.Error)
        assertEquals(
            "El backend respondió con HTTP 503",
            (resultado as ResultadoActualizacionCatalogo.Error).mensaje
        )
    }
}
