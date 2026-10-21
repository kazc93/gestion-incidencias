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

 -- CALCULO FOLIO
 SELECT @v_numero = ISNULL(MAX(IncidenciaId),0) + 1 FROM dbo.Incidencia
 SET @v_folio = 'INC-' + CAST(YEAR(GETDATE()) AS VARCHAR(4)) + '-' + RIGHT('00000' + CAST(@v_numero AS VARCHAR(10)), 5)

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
 -- VALIDACIONES

 SELECT @v_estadoClave = e.Clave
 FROM dbo.Incidencia i
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

 -- INICIAR TRANSACCION
 BEGIN TRANSACTION

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

 BEGIN TRY
 -- VALIDACIONES

 SELECT @v_estadoClave = e.Clave
 FROM dbo.Incidencia i
 INNER JOIN dbo.EstadoIncidencia e
  ON i.EstadoId = e.EstadoId
 WHERE i.IncidenciaId = @idIncidencia
 AND i.Activo = 1

 IF @v_estadoClave IS NULL
 BEGIN
  ;THROW 50001, N'La incidencia no existe',1;
 END

 IF NOT EXISTS(SELECT 1 FROM dbo.Asignacion WHERE IncidenciaId = @idIncidencia AND Activa = 1)
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

 -- OBTENER ESTADOS
 SELECT @v_asignada = EstadoId FROM dbo.EstadoIncidencia WHERE Clave = 'ASIGNADA'
 SELECT @v_enAtencion = EstadoId FROM dbo.EstadoIncidencia WHERE Clave = 'EN_ATENCION'

 -- INICIAR TRANSACCION
 BEGIN TRANSACTION

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

 BEGIN TRY
 -- VALIDACIONES

 SELECT @v_estadoClave = e.Clave
 FROM dbo.Incidencia i
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

 -- OBTENER ESTADOS
 SELECT @v_enAtencion = EstadoId FROM dbo.EstadoIncidencia WHERE Clave = 'EN_ATENCION'
 SELECT @v_resuelta = EstadoId FROM dbo.EstadoIncidencia WHERE Clave = 'RESUELTA'

 -- INICIAR TRANSACCION
 BEGIN TRANSACTION

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