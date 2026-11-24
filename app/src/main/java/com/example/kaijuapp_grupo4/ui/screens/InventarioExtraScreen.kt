package com.example.kaijuapp_grupo4.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.kaijuapp_grupo4.model.Producto
import com.example.kaijuapp_grupo4.ui.theme.AzulLlegadas
import com.example.kaijuapp_grupo4.ui.theme.RojoMerma
import com.example.kaijuapp_grupo4.ui.theme.RojoSuaveFondo
import com.example.kaijuapp_grupo4.ui.theme.RojoSuaveTexto
import com.example.kaijuapp_grupo4.ui.theme.VerdeAzulado
import com.example.kaijuapp_grupo4.ui.theme.VerdeVentas
import com.example.kaijuapp_grupo4.repository.InventarioRepository.ResultadoActualizacionCatalogo
import com.example.kaijuapp_grupo4.viewmodel.InventarioViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventarioExtraScreen(
    navController: NavController,
    inventarioViewModel: InventarioViewModel
) {
    val scope = rememberCoroutineScope()
    val listaSucursales = listOf("San Bernardo", "Maipú", "Persa BioBío")
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val sucursalActual = listaSucursales[selectedTabIndex]

    // Productos reales desde Room DB para la sucursal seleccionada
    var productosSucursalReal by remember { mutableStateOf<List<Producto>>(emptyList()) }

    var textoBusqueda by remember { mutableStateOf("") }
    var cargandoSincronizacion by remember { mutableStateOf(false) }
    var mensajeResultado by remember { mutableStateOf<String?>(null) }
    var errorResultado by remember { mutableStateOf(false) }

    var tiempoTranscurrido by remember { mutableStateOf("Hace un momento") }
    var noDisponible by remember { mutableStateOf(false) }

    // Observar productos reales de la sucursal seleccionada
    LaunchedEffect(selectedTabIndex) {
        inventarioViewModel.repository.observarProductosPorSucursal(sucursalActual)
            .collect { lista ->
                productosSucursalReal = lista
            }
    }

    // Al cambiar de pestaña, consultar estado real del backend
    LaunchedEffect(selectedTabIndex) {
        textoBusqueda = ""
        mensajeResultado = null
        val estadoBackend = inventarioViewModel.repository.obtenerEstadoSincronizacionBackend(sucursalActual)
        if (estadoBackend != null) {
            tiempoTranscurrido = estadoBackend.textoUltimaSincronizacion
                .ifBlank { "Sin registro de actualización" }
            noDisponible = estadoBackend.noDisponible || estadoBackend.estado == "NO_DISPONIBLE"
            errorResultado = false
        } else {
            tiempoTranscurrido = "No disponible"
            noDisponible = false
            mensajeResultado = "No se pudo consultar el estado del backend; se conservan los datos locales."
            errorResultado = true
        }
    }

    val productosFiltrados = remember(productosSucursalReal, textoBusqueda) {
        if (textoBusqueda.isBlank()) {
            productosSucursalReal
        } else {
            productosSucursalReal.filter {
                it.nombre.contains(textoBusqueda, ignoreCase = true) ||
                        it.categoria.contains(textoBusqueda, ignoreCase = true)
            }
        }
    }

    // Conteos reales de movimientos en la sucursal
    val totalUnidades = productosSucursalReal.sumOf { it.cantidadDisponible }
    val mermasContadas = productosSucursalReal.count { it.cantidadDisponible <= 0 }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Inventario Multi-Sucursal") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = VerdeAzulado,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            ScrollableTabRow(
                selectedTabIndex = selectedTabIndex,
                edgePadding = 16.dp,
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ) {
                listaSucursales.forEachIndexed { index, sucursal ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = {
                            Text(
                                text = "Sucursal $sucursal",
                                fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (noDisponible) RojoSuaveFondo else MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Sucursal $sucursalActual",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            if (noDisponible) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(RojoSuaveTexto.copy(alpha = 0.15f))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "No disponible",
                                        color = RojoSuaveTexto,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Última actualización: $tiempoTranscurrido",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Insignias con conteos de movimientos reales
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            InsigniaCambio(
                                texto = "+${productosSucursalReal.size} cargados",
                                colorFondo = AzulLlegadas.copy(alpha = 0.15f),
                                colorTexto = AzulLlegadas
                            )
                            InsigniaCambio(
                                texto = "$totalUnidades stock",
                                colorFondo = VerdeVentas.copy(alpha = 0.15f),
                                colorTexto = VerdeVentas
                            )
                            InsigniaCambio(
                                texto = "-$mermasContadas mermas",
                                colorFondo = RojoMerma.copy(alpha = 0.15f),
                                colorTexto = RojoMerma
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = {
                                scope.launch {
                                    cargandoSincronizacion = true
                                    mensajeResultado = null
                                    try {
                                        when (val resultado =
                                            inventarioViewModel.repository.actualizarCatalogoDesdeBackend(sucursalActual)) {
                                            is ResultadoActualizacionCatalogo.Exito -> {
                                                val estadoBackend = inventarioViewModel.repository
                                                    .obtenerEstadoSincronizacionBackend(sucursalActual)
                                                tiempoTranscurrido = estadoBackend?.textoUltimaSincronizacion
                                                    ?.ifBlank { "Consultado ahora" } ?: "Consultado ahora"
                                                noDisponible = estadoBackend?.noDisponible == true ||
                                                    estadoBackend?.estado == "NO_DISPONIBLE"
                                                mensajeResultado =
                                                    "Catálogo actualizado: ${resultado.productosActualizados} productos recibidos."
                                                errorResultado = false
                                            }
                                            is ResultadoActualizacionCatalogo.Error -> {
                                                mensajeResultado = resultado.mensaje
                                                errorResultado = true
                                            }
                                        }
                                    } finally {
                                        cargandoSincronizacion = false
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = VerdeAzulado),
                            enabled = !cargandoSincronizacion,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (cargandoSincronizacion) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.height(18.dp).width(18.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text("Sincronizando...")
                            } else {
                                Icon(Icons.Default.Refresh, contentDescription = null)
                                Spacer(Modifier.width(6.dp))
                                Text("Actualizar desde backend")
                            }
                        }

                        mensajeResultado?.let { msg ->
                            Text(
                                text = msg,
                                color = if (errorResultado) MaterialTheme.colorScheme.error else VerdeVentas,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }

                // Barra de búsqueda por sucursal
                OutlinedTextField(
                    value = textoBusqueda,
                    onValueChange = { textoBusqueda = it },
                    placeholder = { Text("Buscar producto en $sucursalActual...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Buscar") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (noDisponible) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(RojoSuaveFondo)
                            .padding(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = RojoSuaveTexto
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "Sucursal sin movimientos > 31 días. Su inventario completo permanece totalmente visible a continuación.",
                            color = RojoSuaveTexto,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                Text(
                    text = "Inventario Real de $sucursalActual (${productosFiltrados.size} productos):",
                    style = MaterialTheme.typography.titleSmall
                )

                if (productosFiltrados.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Aún no hay productos registrados en $sucursalActual.\nUsa el perfil o 'Actualizar ahora' para sincronizar.",
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(productosFiltrados, key = { it.id }) { producto ->
                            ItemProductoExtraCard(producto = producto)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InsigniaCambio(texto: String, colorFondo: Color, colorTexto: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(colorFondo)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = texto,
            color = colorTexto,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun ItemProductoExtraCard(producto: Producto) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = producto.nombre,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Categoría: ${producto.categoria} | Sucursal: ${producto.sucursal}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = "${producto.cantidadDisponible} unidades",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
