# Sistema de gestión de incidencias

Proyecto integrador de **Base de Datos II** y **Programación IV**, Ingeniería en Sistemas y TIC, Universidad Panamericana (Chimaltenango), octubre de 2026.

El sistema permite que un empleado reporte un problema con una fotografía, que se asigne a un responsable, que este inicie la atención y que finalmente lo resuelva con una solución y una fotografía final. Todo queda guardado en SQL Server junto con su historial.

## Flujo de estados

```
PENDIENTE  →  ASIGNADA  →  EN_ATENCION  →  RESUELTA
```

Las reglas del flujo se aplican dentro de los procedimientos almacenados de la base de datos.

## Componentes

| Componente | Tecnología | Responsable |
| --- | --- | --- |
| Base de datos | SQL Server (base `incidencias`) | Equipo |
| API REST | ASP.NET Core (.NET 10), proporcionada por el docente | Docente (con una corrección del grupo, ver `api_correcciones`) |
| Aplicación móvil | Android con Kotlin | Equipo |

## Estructura del repositorio

| Carpeta | Contenido |
| --- | --- |
| `base_datos` | Scripts SQL numerados del No. 1 al No. 6 y las fotos de los datos de prueba (`fotos_prueba`) |
| `app_android` | Proyecto de la aplicación Android |
| `api_correcciones` | Controlador corregido de la API (`/api/prioridades`) e instrucciones para publicarla |
| `documentacion` | Diseño, documentación del sistema, manual de usuario y la carpeta `figuras` con los diagramas |
| `pruebas` | Documento de pruebas y capturas de evidencia (carpeta `evidencia`) |

## Instalación de la base de datos

Requisitos: SQL Server 2016 SP1 o superior y SQL Server Management Studio (SSMS).

Ejecutar los scripts de la carpeta `base_datos` **en este orden**, cada uno completo (F5 en SSMS):

| Orden | Script | Qué hace |
| --- | --- | --- |
| 1 | No. 1 | Borra y vuelve a crear la base `incidencias` y crea las 8 tablas |
| 2 | No. 2 | Carga estados, prioridades, tipos de incidencia y empleados |
| 3 | No. 3 | Crea la vista `vw_incidencias` y los índices |
| 4 | No. 4 | Crea los 10 procedimientos almacenados: 4 de operación y 6 de consulta que usa la API |
| 5 | No. 5 | Registra 8 incidencias de prueba y las lleva por el flujo |
| 6 | No. 6 | Consultas de reporte (no modifica datos) |

> **Atención:** el script No. 1 elimina la base `incidencias` si ya existe. Si algo falla a la mitad, se vuelve a ejecutar todo desde el No. 1.

## API

1. Copiar la carpeta publicada de la API en la PC (por ejemplo `C:\APIs\API Incidencias`).
2. En `appsettings.json`, apuntar `ConnectionStrings:DefaultConnection` a la instancia donde se creó la base. El nombre del servidor es el que devuelve `SELECT @@SERVERNAME` en SSMS (en JSON la barra invertida va doble). Si la instancia tiene nombre y está en la misma PC, se puede anteponer `lpc:`.
3. Levantar la API desde su carpeta y dejar la ventana abierta:

   ```
   dotnet GestionIncidencias.Api.dll --urls "http://0.0.0.0:5000"
   ```

4. Verificar `http://localhost:5000/api/health` y `http://localhost:5000/api/health/db`.

La API original no tenía la ruta `/api/prioridades`; la corrección está en la carpeta `api_correcciones`.

## Aplicación Android

1. Abrir la carpeta `app_android` en Android Studio y esperar a que termine la sincronización de Gradle.
2. La URL base está en `data/remote/ApiConfig.kt`: en el emulador usa `http://10.0.2.2:5000/` automáticamente; en un celular físico usa `IP_PC`, que se cambia por la IPv4 de la PC (misma red Wi-Fi).
3. Ejecutar en un emulador o en un celular físico y aceptar los permisos de cámara y de dispositivos cercanos (Android 17 exige este último para conectarse a la PC).

El detalle de instalación, configuración, endpoints, errores y solución de problemas está en la Documentación del sistema, y el uso de cada pantalla en el Manual de usuario.

## Estado del proyecto

| Parte | Estado |
| --- | --- |
| Diseño (modelo entidad-relación, diccionario y flujo de estados) | Terminado |
| Base de datos (6 scripts, 10 procedimientos) | Terminada y probada |
| Pruebas de base de datos (31 casos) | Terminadas, con evidencia |
| Aplicación Android | Terminada y conectada a la API |
| Pruebas de API (10 casos) y de la aplicación (11 casos) | Terminadas: 52 de 52 casos aprobados, con evidencia |
| Documentación y manual de usuario | Terminados |

## Documentación

- Diseño del sistema: carpeta `documentacion`.
- Instalación, configuración, uso, decisiones técnicas y solución de problemas: carpeta `documentacion`.
- Manual de usuario de la aplicación: carpeta `documentacion`.
- Casos de prueba y evidencia: carpeta `pruebas`.

## Equipo

- Kevin Eleazar Azurdia Chamalé (000137188)
- Kendall Nathanael Mux Chex (000150250)
- Jefry Fernando Berduo Pérez (000147971)
- Emerson Josué Vicente Martínez (000151734)


**Docente:** Julio Edgar Barreda Ramírez
