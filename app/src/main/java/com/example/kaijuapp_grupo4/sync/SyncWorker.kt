package com.example.kaijuapp_grupo4.sync

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.kaijuapp_grupo4.data.AppDatabase
import java.util.Calendar
import java.util.concurrent.TimeUnit

class SyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val database = AppDatabase.obtener(applicationContext)
        val sincronizador = SincronizadorInventario(database.operacionPendienteDao())

        val sucursales = listOf("San Bernardo", "Central", "Norte", "Maipú", "Persa BioBío")
        var exitoGlobal = true

        sucursales.forEach { sucursal ->
            val resultado = sincronizador.sincronizarSucursal(sucursal)
            if (resultado is ResultadoSincronizacion.Error) {
                exitoGlobal = false
            }
        }

        return if (exitoGlobal) Result.success() else Result.retry()
    }

    companion object {
        const val UNIQUE_WORK_NAME = "daily-inventory-sync"

        fun programarSincronizacionDiaria1630(context: Context) {
            val ahora = Calendar.getInstance()
            val objetivo1630 = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 16)
                set(Calendar.MINUTE, 30)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            if (ahora.after(objetivo1630)) {
                objetivo1630.add(Calendar.DAY_OF_YEAR, 1)
            }

            val retrasoInicialMillis = objetivo1630.timeInMillis - ahora.timeInMillis

            val restricciones = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val syncRequest = PeriodicWorkRequestBuilder<SyncWorker>(24, TimeUnit.HOURS)
                .setInitialDelay(retrasoInicialMillis, TimeUnit.MILLISECONDS)
                .setConstraints(restricciones)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                UNIQUE_WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                syncRequest
            )
        }
    }
}
