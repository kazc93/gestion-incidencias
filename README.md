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
| API REST | Proporcionada por el docente | Docente |
| Aplicación móvil | Android con Kotlin | Equipo |

## Estructura del repositorio

| Carpeta | Contenido |
| --- | --- |
| `base_datos` | Scripts SQL numerados del No. 1 al No. 6 |
| `app_android` | Proyecto de la aplicación Android |
| `documentacion` | Documentos de diseño y de documentación, y la carpeta `figuras` con los diagramas |
| `pruebas` | Documento de pruebas y capturas de evidencia (carpeta `evidencia`) |

## Instalación de la base de datos

Requisitos: SQL Server 2016 SP1 o superior y SQL Server Management Studio (SSMS).

Ejecutar los scripts de la carpeta `base_datos` **en este orden**, cada uno completo (F5 en SSMS):

| Orden | Script | Qué hace |
| --- | --- | --- |
| 1 | No. 1 | Borra y vuelve a crear la base `incidencias` y crea las 8 tablas |
| 2 | No. 2 | Carga estados, prioridades, tipos de incidencia y empleados |
| 3 | No. 3 | Crea la vista `vw_incidencias` y los índices |
| 4 | No. 4 | Crea los 4 procedimientos almacenados |
| 5 | No. 5 | Registra 8 incidencias de prueba y las lleva por el flujo |
| 6 | No. 6 | Consultas de reporte (no modifica datos) |

> **Atención:** el script No. 1 elimina la base `incidencias` si ya existe. Si algo falla a la mitad, se vuelve a ejecutar todo desde el No. 1.

## Aplicación Android

1. Abrir la carpeta `app_android` en Android Studio y esperar a que termine la sincronización de Gradle.
2. Ejecutar en un emulador o en un celular físico.

La conexión con la API se documentará cuando el docente entregue la URL y el contrato de los endpoints.

## Estado del proyecto

| Parte | Estado |
| --- | --- |
| Diseño (modelo entidad-relación, diccionario y flujo de estados) | Terminado |
| Base de datos (6 scripts) | Terminada y probada |
| Pruebas de base de datos (31 casos) | Terminadas, con evidencia |
| Aplicación Android | En desarrollo |
| API y pruebas de API y aplicación | Pendientes de la entrega de la API |

## Documentación

- Diseño del sistema: carpeta `documentacion`.
- Instalación, configuración, uso y decisiones técnicas: carpeta `documentacion`.
- Casos de prueba y evidencia: carpeta `pruebas`.

## Equipo

- Kevin Eleazar Azurdia Chamalé (000137188)
- Kendall Nathanael Mux Chex (000150250)


**Docente:** Julio Edgar Barreda Ramírez
