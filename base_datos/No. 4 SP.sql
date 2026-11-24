--04 procedimientos almacenados
use incidencias;
GO

--registrar incidencia
CREATE OR ALTER PROCEDURE dbo.sp_registrar_incidencia
(
 @titulo NVARCHAR(120),
 @descripcion NVARCHAR(1000),
 @idTipo INT,
 @idPrioridad INT,
 @idReporta INT,
 @urlFoto NVARCHAR(400),
 @nombreArchivo NVARCHAR(200)
)
AS
BEGIN
 SET NOCOUNT ON
 DECLARE @v_pendiente INT
 DECLARE @v_numero INT
 DECLARE @v_folio VARCHAR(20)
 DECLARE @v_anio VARCHAR(4)
 DECLARE @v_idIncidencia INT

 BEGIN TRY
 -- VALIDACIONES

 IF @titulo IS NULL OR LEN(@titulo) < 5
 BEGIN
  ;THROW 50001, N'El título debe tener al menos 5 caracteres',1;
 END

 IF @descripcion IS NULL OR @descripcion = ''
 BEGIN
  ;THROW 50001, N'La descripción es obligatoria',1;
 END

 IF NOT EXISTS(SELECT 1 FROM dbo.TipoIncidencia WHERE TipoId = @idTipo AND Activo = 1)
 BEGIN
  ;THROW 50001, N'El tipo de incidencia no existe o no está activo',1;
 END

 IF NOT EXISTS(SELECT 1 FROM dbo.Prioridad WHERE PrioridadId = @idPrioridad AND Activo = 1)
 BEGIN
  ;THROW 50001, N'La prioridad no existe o no está activa',1;
 END

 IF NOT EXISTS(SELECT 1 FROM dbo.Empleado WHERE EmpleadoId = @idReporta AND Activo = 1)
 BEGIN
  ;THROW 50001, N'El empleado que reporta no existe o no está activo',1;
 END

 IF @urlFoto IS NULL OR @urlFoto = ''
    OR @nombreArchivo IS NULL OR @nombreArchivo = ''
 BEGIN
  ;THROW 50001, N'No se puede registrar una incidencia sin fotografía inicial',1;
 END

 -- OBTENER ESTADO
 SELECT @v_pendiente = EstadoId FROM dbo.EstadoIncidencia WHERE Clave = 'PENDIENTE'

 -- INICIAR TRANSACCION
 BEGIN TRANSACTION

 -- BLOQUEO PARA QUE DOS REGISTROS NO GENEREN EL MISMO FOLIO
 -- se libera solo al hacer COMMIT o ROLLBACK
 EXEC sp_getapplock
  @Resource = 'folio_incidencia',
  @LockMode = 'Exclusive',
  @LockOwner = 'Transaction'

 -- CALCULO FOLIO (consecutivo por año, reinicia cada año)
 SET @v_anio = CAST(YEAR(SYSDATETIME()) AS VARCHAR(4))

 SELECT @v_numero = ISNULL(MAX(CAST(RIGHT(Folio, 5) AS INT)), 0) + 1
 FROM dbo.Incidencia
 WHERE Folio LIKE 'INC-' + @v_anio + '-%'

 SET @v_folio = 'INC-' + @v_anio + '-' + RIGHT('00000' + CAST(@v_numero AS VARCHAR(10)), 5)

 -- INSERTA INCIDENCIA
 INSERT INTO dbo.Incidencia(
  Folio,
  Titulo,
  Descripcion,
  TipoId,
  PrioridadId,
  EstadoId,
  ReportaEmpleadoId)
 VALUES(
  @v_folio,
  @titulo,
  @descripcion,
  @idTipo,
  @idPrioridad,
  @v_pendiente,
  @idReporta)

 SET @v_idIncidencia = SCOPE_IDENTITY()

 -- INSERTA FOTOGRAFIA
 INSERT INTO dbo.Fotografia(
  IncidenciaId,
  TipoEvidencia,
  UrlAlmacenamiento,
  NombreArchivo)
 VALUES(
  @v_idIncidencia,
  'INICIAL',
  @urlFoto,
  @nombreArchivo)

 -- INSERTA HISTORIAL
 INSERT INTO dbo.HistorialIncidencia(
  IncidenciaId,
  EstadoOrigenId,
  EstadoDestinoId,
  Accion,
  EmpleadoAccionId,
  Comentario)
 VALUES(
  @v_idIncidencia,
  NULL,
  @v_pendiente,
  'REGISTRO',
  @idReporta,
  N'Incidencia registrada')

 COMMIT TRANSACTION;

 SELECT
  @v_idIncidencia AS IncidenciaId,
  @v_folio AS Folio,
  'PENDIENTE' AS Estado,
  'Incidencia registrada correctamente' AS Mensaje
 END TRY

 BEGIN CATCH
  IF XACT_STATE() <> 0
  BEGIN
   ROLLBACK TRANSACTION
  END
  ;THROW
 END CATCH
END
GO

--asignar responsable
CREATE OR ALTER PROCEDURE dbo.sp_asignar_incidencia
(
 @idIncidencia INT,
 @idResponsable INT,
 @idAsigna INT,
 @comentario NVARCHAR(400) = NULL
)
AS
BEGIN
 SET NOCOUNT ON
 DECLARE @v_estadoClave VARCHAR(20)
 DECLARE @v_pendiente INT
 DECLARE @v_asignada INT

 BEGIN TRY
 -- INICIAR TRANSACCION
 -- se abre antes de validar para que nadie cambie el estado mientras tanto
 BEGIN TRANSACTION

 -- VALIDACIONES
 -- UPDLOCK bloquea la fila: si otro usuario asigna al mismo tiempo, espera aquí
 SELECT @v_estadoClave = e.Clave
 FROM dbo.Incidencia i WITH (UPDLOCK, HOLDLOCK)
 INNER JOIN dbo.EstadoIncidencia e
  ON i.EstadoId = e.EstadoId
 WHERE i.IncidenciaId = @idIncidencia
 AND i.Activo = 1

 IF @v_estadoClave IS NULL
 BEGIN
  ;THROW 50001, N'La incidencia no existe',1;
 END

 IF @v_estadoClave <> 'PENDIENTE'
 BEGIN
  ;THROW 50001, N'Solo se puede asignar una incidencia en estado PENDIENTE',1;
 END

 IF NOT EXISTS(SELECT 1 FROM dbo.Empleado WHERE EmpleadoId = @idResponsable AND Activo = 1)
 BEGIN
  ;THROW 50001, N'El responsable no existe o no está activo',1;
 END

 IF NOT EXISTS(SELECT 1 FROM dbo.Empleado WHERE EmpleadoId = @idAsigna AND Activo = 1)
 BEGIN
  ;THROW 50001, N'El empleado que asigna no existe o no está activo',1;
 END

 -- OBTENER ESTADOS
 SELECT @v_pendiente = EstadoId FROM dbo.EstadoIncidencia WHERE Clave = 'PENDIENTE'
 SELECT @v_asignada = EstadoId FROM dbo.EstadoIncidencia WHERE Clave = 'ASIGNADA'

 -- INSERTA ASIGNACION
 INSERT INTO dbo.Asignacion(
  IncidenciaId,
  ResponsableEmpleadoId,
  AsignaEmpleadoId,
  Comentario)
 VALUES(
  @idIncidencia,
  @idResponsable,
  @idAsigna,
  @comentario)

 -- ACTUALIZA ESTADO
 UPDATE dbo.Incidencia
 SET EstadoId = @v_asignada
 WHERE IncidenciaId = @idIncidencia

 -- INSERTA HISTORIAL
 INSERT INTO dbo.HistorialIncidencia(
  IncidenciaId,
  EstadoOrigenId,
  EstadoDestinoId,
  Accion,
  EmpleadoAccionId,
  Comentario)
 VALUES(
  @idIncidencia,
  @v_pendiente,
  @v_asignada,
  'ASIGNACION',
  @idAsigna,
  @comentario)

 COMMIT TRANSACTION;

 SELECT
  @idIncidencia AS IncidenciaId,
  'ASIGNADA' AS Estado,
  'Incidencia asignada correctamente' AS Mensaje
 END TRY

 BEGIN CATCH
  IF XACT_STATE() <> 0
  BEGIN
   ROLLBACK TRANSACTION
  END
  ;THROW
 END CATCH
END
GO

--iniciar atencion
CREATE OR ALTER PROCEDURE dbo.sp_iniciar_atencion
(
 @idIncidencia INT,
 @idEmpleado INT
)
AS
BEGIN
 SET NOCOUNT ON
 DECLARE @v_estadoClave VARCHAR(20)
 DECLARE @v_asignada INT
 DECLARE @v_enAtencion INT
 DECLARE @v_idResponsable INT

 BEGIN TRY
 -- INICIAR TRANSACCION
 -- se abre antes de validar para que nadie cambie el estado mientras tanto
 BEGIN TRANSACTION

 -- VALIDACIONES
 -- UPDLOCK bloquea la fila: si otro usuario la modifica al mismo tiempo, espera aquí
 SELECT @v_estadoClave = e.Clave
 FROM dbo.Incidencia i WITH (UPDLOCK, HOLDLOCK)
 INNER JOIN dbo.EstadoIncidencia e
  ON i.EstadoId = e.EstadoId
 WHERE i.IncidenciaId = @idIncidencia
 AND i.Activo = 1

 IF @v_estadoClave IS NULL
 BEGIN
  ;THROW 50001, N'La incidencia no existe',1;
 END

 SELECT @v_idResponsable = ResponsableEmpleadoId
 FROM dbo.Asignacion
 WHERE IncidenciaId = @idIncidencia
 AND Activa = 1

 IF @v_idResponsable IS NULL
 BEGIN
  ;THROW 50001, N'No se puede iniciar la atención porque la incidencia no tiene responsable asignado',1;
 END

 IF @v_estadoClave <> 'ASIGNADA'
 BEGIN
  ;THROW 50001, N'Solo una incidencia ASIGNADA puede pasar a EN ATENCION',1;
 END

 IF NOT EXISTS(SELECT 1 FROM dbo.Empleado WHERE EmpleadoId = @idEmpleado AND Activo = 1)
 BEGIN
  ;THROW 50001, N'El empleado no existe o no está activo',1;
 END

 IF @idEmpleado <> @v_idResponsable
 BEGIN
  ;THROW 50001, N'Solo el responsable asignado puede iniciar la atención',1;
 END

 -- OBTENER ESTADOS
 SELECT @v_asignada = EstadoId FROM dbo.EstadoIncidencia WHERE Clave = 'ASIGNADA'
 SELECT @v_enAtencion = EstadoId FROM dbo.EstadoIncidencia WHERE Clave = 'EN_ATENCION'

 -- ACTUALIZA ESTADO
 UPDATE dbo.Incidencia
 SET EstadoId = @v_enAtencion
 WHERE IncidenciaId = @idIncidencia

 -- INSERTA HISTORIAL
 INSERT INTO dbo.HistorialIncidencia(
  IncidenciaId,
  EstadoOrigenId,
  EstadoDestinoId,
  Accion,
  EmpleadoAccionId,
  Comentario)
 VALUES(
  @idIncidencia,
  @v_asignada,
  @v_enAtencion,
  'INICIO_ATENCION',
  @idEmpleado,
  N'Inicio de atención')

 COMMIT TRANSACTION;

 SELECT
  @idIncidencia AS IncidenciaId,
  'EN_ATENCION' AS Estado,
  'Atención iniciada correctamente' AS Mensaje
 END TRY

 BEGIN CATCH
  IF XACT_STATE() <> 0
  BEGIN
   ROLLBACK TRANSACTION
  END
  ;THROW
 END CATCH
END
GO

--resolver incidencia
CREATE OR ALTER PROCEDURE dbo.sp_resolver_incidencia
(
 @idIncidencia INT,
 @idEmpleado INT,
 @solucion NVARCHAR(1000),
 @urlFoto NVARCHAR(400),
 @nombreArchivo NVARCHAR(200)
)
AS
BEGIN
 SET NOCOUNT ON
 DECLARE @v_estadoClave VARCHAR(20)
 DECLARE @v_enAtencion INT
 DECLARE @v_resuelta INT
 DECLARE @v_idResponsable INT

 BEGIN TRY
 -- INICIAR TRANSACCION
 -- se abre antes de validar para que nadie cambie el estado mientras tanto
 BEGIN TRANSACTION

 -- VALIDACIONES
 -- UPDLOCK bloquea la fila: si otro usuario la resuelve al mismo tiempo, espera aquí
 SELECT @v_estadoClave = e.Clave
 FROM dbo.Incidencia i WITH (UPDLOCK, HOLDLOCK)
 INNER JOIN dbo.EstadoIncidencia e
  ON i.EstadoId = e.EstadoId
 WHERE i.IncidenciaId = @idIncidencia
 AND i.Activo = 1

 IF @v_estadoClave IS NULL
 BEGIN
  ;THROW 50001, N'La incidencia no existe',1;
 END

 IF @v_estadoClave <> 'EN_ATENCION'
 BEGIN
  ;THROW 50001, N'Solo una incidencia EN ATENCION puede pasar a RESUELTA',1;
 END

 IF @solucion IS NULL OR @solucion = ''
 BEGIN
  ;THROW 50001, N'La resolución exige una descripción de la solución',1;
 END

 IF @urlFoto IS NULL OR @urlFoto = ''
    OR @nombreArchivo IS NULL OR @nombreArchivo = ''
 BEGIN
  ;THROW 50001, N'La resolución exige una fotografía final',1;
 END

 IF NOT EXISTS(SELECT 1 FROM dbo.Empleado WHERE EmpleadoId = @idEmpleado AND Activo = 1)
 BEGIN
  ;THROW 50001, N'El empleado no existe o no está activo',1;
 END

 SELECT @v_idResponsable = ResponsableEmpleadoId
 FROM dbo.Asignacion
 WHERE IncidenciaId = @idIncidencia
 AND Activa = 1

 IF @v_idResponsable IS NULL OR @idEmpleado <> @v_idResponsable
 BEGIN
  ;THROW 50001, N'Solo el responsable asignado puede resolver la incidencia',1;
 END

 -- OBTENER ESTADOS
 SELECT @v_enAtencion = EstadoId FROM dbo.EstadoIncidencia WHERE Clave = 'EN_ATENCION'
 SELECT @v_resuelta = EstadoId FROM dbo.EstadoIncidencia WHERE Clave = 'RESUELTA'

 -- ACTUALIZA INCIDENCIA
 UPDATE dbo.Incidencia
 SET EstadoId = @v_resuelta,
  Solucion = @solucion,
  FechaResolucion = GETDATE()
 WHERE IncidenciaId = @idIncidencia

 -- INSERTA FOTOGRAFIA
 INSERT INTO dbo.Fotografia(
  IncidenciaId,
  TipoEvidencia,
  UrlAlmacenamiento,
  NombreArchivo)
 VALUES(
  @idIncidencia,
  'FINAL',
  @urlFoto,
  @nombreArchivo)

 -- INSERTA HISTORIAL
 INSERT INTO dbo.HistorialIncidencia(
  IncidenciaId,
  EstadoOrigenId,
  EstadoDestinoId,
  Accion,
  EmpleadoAccionId,
  Comentario)
 VALUES(
  @idIncidencia,
  @v_enAtencion,
  @v_resuelta,
  'RESOLUCION',
  @idEmpleado,
  N'Incidencia resuelta')

 COMMIT TRANSACTION;

 SELECT
  @idIncidencia AS IncidenciaId,
  'RESUELTA' AS Estado,
  'Incidencia resuelta correctamente' AS Mensaje
 END TRY

 BEGIN CATCH
  IF XACT_STATE() <> 0
  BEGIN
   ROLLBACK TRANSACTION
  END
  ;THROW
 END CATCH
END
GO

--procedimientos de consulta para la API
--la API del docente llama estos procedimientos para los catalogos, el listado y el detalle
--los nombres y las columnas que devuelven son los que espera la API (no cambiarlos)

--catalogo de estados (columnas: Id, Clave, Nombre)
CREATE OR ALTER PROCEDURE dbo.sp_listar_estados
AS
BEGIN
 SET NOCOUNT ON

 SELECT
  EstadoId AS Id,
  Clave,
  Nombre
 FROM dbo.EstadoIncidencia
 WHERE Activo = 1
 ORDER BY Orden
END
GO

--catalogo de tipos de incidencia
CREATE OR ALTER PROCEDURE dbo.sp_listar_tipos
AS
BEGIN
 SET NOCOUNT ON

 SELECT
  TipoId AS Id,
  Clave,
  Nombre
 FROM dbo.TipoIncidencia
 WHERE Activo = 1
 ORDER BY Nombre
END
GO

--catalogo de prioridades, de la mas baja a la mas alta
CREATE OR ALTER PROCEDURE dbo.sp_listar_prioridades
AS
BEGIN
 SET NOCOUNT ON

 SELECT
  PrioridadId AS Id,
  Clave,
  Nombre
 FROM dbo.Prioridad
 WHERE Activo = 1
 ORDER BY Nivel
END
GO

--catalogo de empleados
--Clave es el numero de empleado y Nombre es el nombre completo
CREATE OR ALTER PROCEDURE dbo.sp_listar_empleados
AS
BEGIN
 SET NOCOUNT ON

 SELECT
  EmpleadoId AS Id,
  CAST(NumeroEmpleado AS NVARCHAR(20)) AS Clave,
  CONCAT(Nombre, N' ', Apellidos) AS Nombre
 FROM dbo.Empleado
 WHERE Activo = 1
 ORDER BY Nombre, Apellidos
END
GO

--listado de incidencias con filtros opcionales por estado y por tipo
--usa la vista vw_incidencias del script No. 3
CREATE OR ALTER PROCEDURE dbo.sp_listar_incidencias
(
 @estadoId INT = NULL,
 @tipoId INT = NULL
)
AS
BEGIN
 SET NOCOUNT ON

 SELECT
  v.IncidenciaId AS Id,
  v.Folio,
  v.Titulo,
  v.Tipo,
  v.Prioridad,
  v.EstadoId,
  v.EstadoClave,
  v.Estado,
  v.FechaRegistro,
  v.Reporta,
  v.Responsable
 FROM dbo.vw_incidencias v
 WHERE (@estadoId IS NULL OR v.EstadoId = @estadoId)
 AND (@tipoId IS NULL OR v.TipoId = @tipoId)
 ORDER BY v.FechaRegistro DESC, v.IncidenciaId DESC
END
GO

--detalle de una incidencia
--devuelve 3 resultados en este orden: datos generales, fotografias e historial
--si la incidencia no existe lanza el error 50002, que la API devuelve como 404
CREATE OR ALTER PROCEDURE dbo.sp_obtener_incidencia
(
 @idIncidencia INT
)
AS
BEGIN
 SET NOCOUNT ON

 IF NOT EXISTS(SELECT 1 FROM dbo.Incidencia WHERE IncidenciaId = @idIncidencia AND Activo = 1)
 BEGIN
  ;THROW 50002, N'La incidencia no existe',1;
 END

 -- 1. DATOS GENERALES
 SELECT
  i.IncidenciaId AS Id,
  i.Folio,
  i.Titulo,
  i.Descripcion,
  t.Nombre AS Tipo,
  p.Nombre AS Prioridad,
  e.EstadoId,
  e.Clave AS EstadoClave,
  e.Nombre AS Estado,
  i.FechaRegistro,
  CONCAT(r.Nombre, N' ', r.Apellidos) AS Reporta,
  a.ResponsableEmpleadoId AS ResponsableId,
  re.Nombre + N' ' + re.Apellidos AS Responsable,
  i.Solucion,
  i.FechaResolucion
 FROM dbo.Incidencia i
 INNER JOIN dbo.TipoIncidencia t
  ON i.TipoId = t.TipoId
 INNER JOIN dbo.Prioridad p
  ON i.PrioridadId = p.PrioridadId
 INNER JOIN dbo.EstadoIncidencia e
  ON i.EstadoId = e.EstadoId
 INNER JOIN dbo.Empleado r
  ON i.ReportaEmpleadoId = r.EmpleadoId
 LEFT JOIN dbo.Asignacion a
  ON i.IncidenciaId = a.IncidenciaId
  AND a.Activa = 1
 LEFT JOIN dbo.Empleado re
  ON a.ResponsableEmpleadoId = re.EmpleadoId
 WHERE i.IncidenciaId = @idIncidencia

 -- 2. FOTOGRAFIAS (INICIAL y FINAL)
 SELECT
  f.TipoEvidencia,
  f.UrlAlmacenamiento AS Url,
  f.NombreArchivo,
  f.FechaCaptura
 FROM dbo.Fotografia f
 WHERE f.IncidenciaId = @idIncidencia
 ORDER BY f.FechaCaptura, f.FotografiaId

 -- 3. HISTORIAL
 SELECT
  h.Accion,
  eo.Nombre AS EstadoOrigen,
  ed.Nombre AS EstadoDestino,
  CONCAT(em.Nombre, N' ', em.Apellidos) AS HechoPor,
  h.Comentario,
  h.FechaAccion
 FROM dbo.HistorialIncidencia h
 LEFT JOIN dbo.EstadoIncidencia eo
  ON h.EstadoOrigenId = eo.EstadoId
 INNER JOIN dbo.EstadoIncidencia ed
  ON h.EstadoDestinoId = ed.EstadoId
 INNER JOIN dbo.Empleado em
  ON h.EmpleadoAccionId = em.EmpleadoId
 WHERE h.IncidenciaId = @idIncidencia
 ORDER BY h.FechaAccion, h.HistorialId
END
GO

--verificacion final
--deben aparecer los 10 procedimientos
SELECT name AS Procedimiento
FROM sys.procedures
ORDER BY name;
GO
