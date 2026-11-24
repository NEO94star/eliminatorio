package com.example.kaijuapp_grupo4

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.kaijuapp_grupo4.navigation.AppNavigation
import com.example.kaijuapp_grupo4.sync.SyncWorker
import com.example.kaijuapp_grupo4.ui.theme.KaijuApp_Grupo4Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Programar sincronización diaria a las 16:30 hora local con WorkManager
        try {
            SyncWorker.programarSincronizacionDiaria1630(applicationContext)
        } catch (_: Exception) {
            // Manejo defensivo en entornos de pruebas
        }

        setContent {
            KaijuApp_Grupo4Theme {
                AppNavigation()
            }
        }
    }
}
