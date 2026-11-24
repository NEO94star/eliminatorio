# KaijuApp - Sistema de Control de Inventario y Ventas Multi-Sucursal

**Asignatura:** Desarrollo de Aplicaciones Móviles (DSY1105)  
**Evaluación:** Parcial 4 - Encargo Estudiante (6% Ponderación)  
**Institución:** Duoc UC - 2025  

---

## 👥 1. Integrantes del Equipo
* **Grupo:** Grupo 4
* **Desarrolladores:** Equipo KaijuApp

---

## 📱 2. Descripción y Funcionalidades del Proyecto

**KaijuApp** es una solución móvil nativa en Kotlin desarrollada con **Jetpack Compose** para la gestión inteligente de inventario y ventas en tiendas. Opera bajo una arquitectura **Offline-First**, manteniendo persistencia local inmediata con **Room Database** y sincronización por lotes controlada mediante **WorkManager** y **Retrofit** con un **Backend Spring Boot** desplegado en una instancia AWS EC2.

### Principales Funcionalidades:
* **Inventario Multi-Sucursal ("Inventario Extra"):**
  * Pestañas superiores para navegar entre sucursales (*San Bernardo*, *Central*, *Norte*, *Maipú*, *Persa BioBío*).
  * Marca de tiempo dinámica de última actualización (*"Hace menos de 1 minuto"*, *"Hace X minutos"*, *"Hace X horas"*, *"Hace X días"*, *"Hace X semanas"*).
  * Estado `NO_DISPONIBLE` en **rojo suave** si una sucursal supera los 31 días sin actualización. **Garantía:** El inventario completo permanece totalmente visible desde Room sin bloquearse.
  * Insignias informativas de cambios (Deltas): 🟦 `+X llegados` (Azul), 🟩 `-X vendidos` (Verde), 🟥 `-X merma` (Rojo).
  * Botón **"Actualizar ahora"**: Ejecuta la sincronización manual del lote a EC2 en tiempo real.
  * Barra de búsqueda por sucursal para filtrar productos por nombre o categoría.
* **Sincronización Automática con WorkManager (`SyncWorker`):**
  * Tarea programada diariamente a las **16:30 hora de Chile** con el nombre único `"daily-inventory-sync"`.
  * Cola de operaciones pendientes en Room (`operaciones_pendientes`) con acciones `ADD`, `UPDATE`, `SALE`, `DELETE`, `UPSERT`.
  * Las operaciones locales no se borran hasta que el backend confirme `operacionesAplicadas`.
* **Resumen de Ventas con API Externa:** Consulta de tasas de cambio en vivo con **Frankfurter API** (`https://api.frankfurter.dev/v1/`) para conversión de CLP a USD/EUR.
* **Orientación Fija Portrait:** Bloqueada verticalmente (`portrait`) en `AndroidManifest.xml`.
* **Pruebas Unitarias (JUnit + MockK):** Suite de tests automatizados con cobertura $\ge 80\%$.

---

## 🔌 3. Endpoints Utilizados

### 🟢 Backend Propio (Spring Boot en AWS EC2)
* **Base URL:** Configurada vía `BuildConfig.BASE_URL` (`http://184.192.5.101:8080/`)
* `POST /api/sincronizacion/lote` - Enviar lote de operaciones pendientes por sucursal.
* `GET /api/sincronizacion/{sucursal}` - Consultar estado de sincronización y fecha dinámica de la sucursal.
* `GET /api/sucursales` - Obtener lista de sucursales disponibles.
* `GET /api/productos?sucursal={nombre}` - Filtrar inventario por sucursal.
* `GET /api/sucursales/{sucursal}/stock-bajo` - Consultar productos con stock bajo en la sucursal.
* `GET /api/productos` - Listar todos los productos del sistema central.

### 🌐 API Externa
* **Base URL:** `https://api.frankfurter.dev/v1/`
* `GET /v1/latest?from=USD` - Obtiene las tasas de cambio actualizadas en tiempo real (EUR, CLP, USD).

---

## 🛠️ 4. Pasos para Ejecutar el Proyecto

### Requisitos Previos:
* Android Studio Ladybug / Koala o superior.
* JDK 17 o superior.
* Emulador Android o dispositivo físico (Android 9.0 API 28+).

### Pasos de Ejecución:

#### 1. Ejecutar la App Móvil Android (`app/`):
1. Abrir la carpeta raíz del proyecto en Android Studio.
2. Seleccionar la configuración de ejecución `app`.
3. Iniciar en emulador Android o dispositivo conectado.
4. La URL del backend apuntará a la Elastic IP de AWS `http://184.192.5.101:8080/`.

#### 2. Ejecutar la Suite de Pruebas Unitarias:
```bash
./gradlew testDebugUnitTest
```

---

## 🔑 5. Evidencia del APK Firmado y KeyStore (`.jks`)

El archivo ejecutable APK ha sido configurado y firmado digitalmente según los estándares requeridos en `app/build.gradle.kts`.

* **Ubicación de la Clave de Firma:** `app/release.jks`
* **Alias de la Clave:** `kaijuKey`
* **Algoritmo de Cifrado:** RSA 2048-bit (Validez 10,000 días)

---

## 📊 6. Planificación y Trello
La distribución de tareas, historias de usuario y planificación ágil del equipo se encuentra documentada y sincronizada en el tablero de Trello del proyecto.
