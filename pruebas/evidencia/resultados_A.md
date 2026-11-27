# Resultados de las pruebas de API (A01–A10)

- Fecha: 2026-11-23
- API: `http://localhost:5000` (GestionIncidencias.Api, Kestrel)
- Base de datos: `incidencias` en `JoshuasPC\MSSQLSERVER01`, recreada con los scripts No. 1 al No. 6 justo antes de las pruebas (ver `scripts.txt`)
- Herramienta: `curl.exe`. La evidencia de cada caso (petición exacta, código HTTP, cabeceras y cuerpo) está en `<ID>.txt`.

| ID | Resultado esperado | Resultado obtenido | Estado |
|----|--------------------|--------------------|--------|
| A01 | 200 en estados, tipos, prioridades y empleados, con 4, 7, 4 y 6 registros | 200 OK en las 4 rutas, con 4 estados, 7 tipos, 4 prioridades y 6 empleados | Aprobado |
| A02 | `GET /api/incidencias` responde 200 con 8 incidencias | 200 OK con 8 incidencias (ids 1 a 8) | Aprobado |
| A03 | `estadoId=1` devuelve 7 y 8; `tipoId=2` devuelve 3 y 7 | 200 OK. `estadoId=1` devolvió los ids 8 y 7; `tipoId=2` devolvió los ids 7 y 3 | Aprobado |
| A04 | `GET /api/incidencias/1` trae los datos, el responsable Luis Hernández Cruz, la solución, 2 fotos y 4 movimientos de historial | 200 OK con responsable "Luis Hernández Cruz", solución "Se cambió la lámpara por una nueva", 2 fotos (INICIAL y FINAL) y 4 movimientos (REGISTRO, ASIGNACION, INICIO_ATENCION, RESOLUCION) | Aprobado |
| A05 | `POST` multipart responde 201 con incidenciaId 9, folio INC-2026-00009 y estado PENDIENTE; la foto de `GET /9` responde 200 | 201 Created con `incidenciaId` 9, `folio` "INC-2026-00009" y `estado` "PENDIENTE". `GET /9` devuelve la foto INICIAL en `http://localhost:5000/fotos/8123f0fb….jpg`, que responde 200 `image/jpeg` (693 bytes) | Aprobado |
| A06 | asignar, iniciar y resolver la 9 responden 200 con ASIGNADA, EN_ATENCION y RESUELTA | asignar → 200 "ASIGNADA"; iniciar → 200 "EN_ATENCION"; resolver (multipart con foto) → 200 "RESUELTA" | Aprobado |
| A07 | `POST /7/iniciar` responde 400 "No se puede iniciar la atención porque la incidencia no tiene responsable asignado" | 400 Bad Request con exactamente ese mensaje | Aprobado |
| A08 | `GET /api/incidencias/999` responde 404 "La incidencia no existe" | 404 Not Found con "La incidencia no existe" | Aprobado |
| A09 | `POST` con título "Hola" y `POST` con un archivo que no es JPG responden 400 | Título "Hola" → 400 "Los datos enviados no son válidos. Revise los campos y sus tipos". Archivo .txt → 400 "La fotografía debe estar en formato JPG". No se creó ninguna incidencia: siguen siendo 9 | Aprobado |
| A10 | Con SQL Server detenido, `GET /api/incidencias` responde 500 sin detalles de SQL | No ejecutada: la consola no tiene permisos de administrador y `Stop-Service` fue denegado (ver `A10.txt`) | No ejecutada |

## Observaciones

- **A04.** La foto INICIAL de los datos de prueba (incidencia 1) viene como ruta relativa (`/fotos/inc1_inicial.jpg`). Las fotos subidas por la API (incidencia 9) vienen como URL absoluta (`http://localhost:5000/fotos/...`). Las dos funcionan.
- **A05.** En el registro se envió `idTipo=1`, y la API lo guardó como "Red e internet", que es el id 1 del catálogo. Es lo esperado según el catálogo.
- **A06.** En las respuestas de asignar, iniciar y resolver, `folio` llega como `null`. Solo el registro lo devuelve.
- **A09.** El 400 del título corto devuelve un mensaje genérico ("Los datos enviados no son válidos…") en lugar de uno que diga que el título es demasiado corto. El código HTTP es el esperado.
- **A10.** Se puede ejecutar a mano desde una PowerShell como administrador. Los pasos están en `A10.txt`.

**Totales:** 9 Aprobadas, 0 Fallidas, 1 No ejecutada.
