package com.example.kaijuapp_grupo4.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.kaijuapp_grupo4.model.Movimiento
import com.example.kaijuapp_grupo4.model.OperacionPendiente
import com.example.kaijuapp_grupo4.model.Producto

@Database(entities = [Producto::class, Movimiento::class, OperacionPendiente::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun productoDao(): ProductoDao
    abstract fun movimientoDao(): MovimientoDao
    abstract fun operacionPendienteDao(): OperacionPendienteDao

    companion object {
        @Volatile
        private var instancia: AppDatabase? = null

        fun obtener(context: Context): AppDatabase {
            return instancia ?: synchronized(this) {
                instancia ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "kaiju_db"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            db.execSQL("INSERT INTO productos (id, nombre, categoria, precio, cantidadDisponible, stockMinimo, imagenUri, sucursal) VALUES ('1', 'Polera Kaiju Classic', 'Ropa', 15990.0, 12, 5, NULL, 'San Bernardo')")
                            db.execSQL("INSERT INTO productos (id, nombre, categoria, precio, cantidadDisponible, stockMinimo, imagenUri, sucursal) VALUES ('2', 'Figura Godzilla 15cm', 'Coleccionables', 29990.0, 3, 5, NULL, 'San Bernardo')")
                            db.execSQL("INSERT INTO productos (id, nombre, categoria, precio, cantidadDisponible, stockMinimo, imagenUri, sucursal) VALUES ('3', 'Taza Logo Kaiju', 'Accesorios', 7990.0, 20, 10, NULL, 'San Bernardo')")
                        }
                    })
                    .build()
                    .also { instancia = it }
            }
        }
    }
}
