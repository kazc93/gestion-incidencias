# Corrección a la API del docente

La API entregada tenía `PrioridadesController.cs` como una plantilla vacía de Visual Studio (sin `[Route]` ni `[HttpGet]`), por lo que `GET /api/prioridades` respondía 404. El repositorio de la API ya tenía `ObtenerPrioridadesAsync()` y el procedimiento `sp_listar_prioridades` existe en la base, así que solo faltaba el controlador.

`PrioridadesController.cs` de esta carpeta es el controlador corregido, hecho igual que `TiposController`.

## Cómo aplicarla

Requisito: SDK de .NET 10 (`dotnet --list-sdks`).

1. Reemplazar `GestionIncidencias.Api/Controllers/PrioridadesController.cs` del código fuente de la API por el de esta carpeta.
2. En `appsettings.json` del código fuente, poner la cadena de conexión de la PC (ver la Documentación del sistema).
3. Detener la API si está corriendo (Ctrl+C).
4. Publicar sobre la carpeta donde corre la API:

   ```
   cd GestionIncidencias.Api
   dotnet publish -c Release -o "C:\APIs\API Incidencias\API Incidencias"
   ```

5. Levantar la API y verificar `http://localhost:5000/api/prioridades`: debe responder 200 con las 4 prioridades.
