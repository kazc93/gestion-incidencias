--01 creacion de la base y tablas
--reinicio de la base
USE master;
GO

IF DB_ID(N'incidencias') IS NOT NULL
    DROP DATABASE incidencias;
GO

CREATE DATABASE incidencias;
GO

USE incidencias;
GO

--empleado
CREATE TABLE dbo.Empleado (
    EmpleadoId     INT IDENTITY(1,1) NOT NULL,
    NumeroEmpleado VARCHAR(20)   NOT NULL,
    Nombre         NVARCHAR(80)  NOT NULL,
    Apellidos      NVARCHAR(120) NOT NULL,
    Correo         NVARCHAR(120) NOT NULL,
    Activo         BIT NOT NULL CONSTRAINT DF_Empleado_Activo DEFAULT (1),
    CONSTRAINT PK_Empleado PRIMARY KEY (EmpleadoId),
    CONSTRAINT UQ_Empleado_Numero UNIQUE (NumeroEmpleado),
    CONSTRAINT UQ_Empleado_Correo UNIQUE (Correo)
);
GO

--tipo de incidencia
CREATE TABLE dbo.TipoIncidencia (
    TipoId  INT IDENTITY(1,1) NOT NULL,
    Clave   VARCHAR(20)  NOT NULL,
    Nombre  NVARCHAR(80) NOT NULL,
    Activo  BIT NOT NULL CONSTRAINT DF_Tipo_Activo DEFAULT (1),
    CONSTRAINT PK_TipoIncidencia PRIMARY KEY (TipoId),
    CONSTRAINT UQ_Tipo_Clave UNIQUE (Clave)
);
GO

--prioridad
CREATE TABLE dbo.Prioridad (
    PrioridadId INT IDENTITY(1,1) NOT NULL,
    Clave       VARCHAR(20)  NOT NULL,
    Nombre      NVARCHAR(40) NOT NULL,
    Nivel       TINYINT NOT NULL,
    Activo      BIT NOT NULL CONSTRAINT DF_Prioridad_Activo DEFAULT (1),
    CONSTRAINT PK_Prioridad PRIMARY KEY (PrioridadId),
    CONSTRAINT UQ_Prioridad_Clave UNIQUE (Clave),
    CONSTRAINT CK_Prioridad_Nivel CHECK (Nivel BETWEEN 1 AND 4)
);
GO

--estado de incidencia
CREATE TABLE dbo.EstadoIncidencia (
    EstadoId INT IDENTITY(1,1) NOT NULL,
    Clave    VARCHAR(20)  NOT NULL,
    Nombre   NVARCHAR(40) NOT NULL,
    Orden    TINYINT NOT NULL,
    Activo   BIT NOT NULL CONSTRAINT DF_Estado_Activo DEFAULT (1),
    CONSTRAINT PK_EstadoIncidencia PRIMARY KEY (EstadoId),
    CONSTRAINT UQ_Estado_Clave UNIQUE (Clave)
);
GO

--verificar tablas de catalogo
SELECT name FROM sys.tables ORDER BY name;
GO

--incidencia
CREATE TABLE dbo.Incidencia (
    IncidenciaId       INT IDENTITY(1,1) NOT NULL,
    Folio              VARCHAR(20)    NOT NULL,
    Titulo             NVARCHAR(120)  NOT NULL,
    Descripcion        NVARCHAR(1000) NOT NULL,
    TipoId             INT NOT NULL,
    PrioridadId        INT NOT NULL,
    EstadoId           INT NOT NULL,
    ReportaEmpleadoId  INT NOT NULL,
    FechaRegistro      DATETIME2(0) NOT NULL
        CONSTRAINT DF_Incidencia_FechaRegistro DEFAULT (SYSDATETIME()),
    Solucion           NVARCHAR(1000) NULL,
    FechaResolucion    DATETIME2(0)   NULL,
    Activo             BIT NOT NULL
        CONSTRAINT DF_Incidencia_Activo DEFAULT (1),

    CONSTRAINT PK_Incidencia PRIMARY KEY (IncidenciaId),
    CONSTRAINT UQ_Incidencia_Folio UNIQUE (Folio),

    CONSTRAINT FK_Incidencia_Tipo
        FOREIGN KEY (TipoId) REFERENCES dbo.TipoIncidencia (TipoId),
    CONSTRAINT FK_Incidencia_Prioridad
        FOREIGN KEY (PrioridadId) REFERENCES dbo.Prioridad (PrioridadId),
    CONSTRAINT FK_Incidencia_Estado
        FOREIGN KEY (EstadoId) REFERENCES dbo.EstadoIncidencia (EstadoId),
    CONSTRAINT FK_Incidencia_Reporta
        FOREIGN KEY (ReportaEmpleadoId) REFERENCES dbo.Empleado (EmpleadoId),

    CONSTRAINT CK_Incidencia_Titulo
        CHECK (LEN(Titulo) >= 5)
);
GO

--asignacion
CREATE TABLE dbo.Asignacion (
    AsignacionId          INT IDENTITY(1,1) NOT NULL,
    IncidenciaId          INT NOT NULL,
    ResponsableEmpleadoId INT NOT NULL,
    AsignaEmpleadoId      INT NOT NULL,
    FechaAsignacion       DATETIME2(0) NOT NULL
        CONSTRAINT DF_Asignacion_Fecha DEFAULT (SYSDATETIME()),
    Comentario            NVARCHAR(400) NULL,
    Activa                BIT NOT NULL
        CONSTRAINT DF_Asignacion_Activa DEFAULT (1),

    CONSTRAINT PK_Asignacion PRIMARY KEY (AsignacionId),
    CONSTRAINT FK_Asignacion_Incidencia
        FOREIGN KEY (IncidenciaId) REFERENCES dbo.Incidencia (IncidenciaId),
    CONSTRAINT FK_Asignacion_Responsable
        FOREIGN KEY (ResponsableEmpleadoId) REFERENCES dbo.Empleado (EmpleadoId),
    CONSTRAINT FK_Asignacion_Asigna
        FOREIGN KEY (AsignaEmpleadoId) REFERENCES dbo.Empleado (EmpleadoId)
);
GO

--fotografia
CREATE TABLE dbo.Fotografia (
    FotografiaId      INT IDENTITY(1,1) NOT NULL,
    IncidenciaId      INT NOT NULL,
    TipoEvidencia     VARCHAR(10) NOT NULL,
    UrlAlmacenamiento NVARCHAR(400) NOT NULL,
    NombreArchivo     NVARCHAR(200) NOT NULL,
    FechaCaptura      DATETIME2(0) NOT NULL
        CONSTRAINT DF_Fotografia_Fecha DEFAULT (SYSDATETIME()),

    CONSTRAINT PK_Fotografia PRIMARY KEY (FotografiaId),
    CONSTRAINT FK_Fotografia_Incidencia
        FOREIGN KEY (IncidenciaId) REFERENCES dbo.Incidencia (IncidenciaId),
    CONSTRAINT CK_Fotografia_Tipo
        CHECK (TipoEvidencia IN ('INICIAL', 'FINAL'))
);
GO

--historial de incidencia
CREATE TABLE dbo.HistorialIncidencia (
    HistorialId      INT IDENTITY(1,1) NOT NULL,
    IncidenciaId     INT NOT NULL,
    EstadoOrigenId   INT NULL,
    EstadoDestinoId  INT NOT NULL,
    Accion           VARCHAR(30) NOT NULL,
    EmpleadoAccionId INT NOT NULL,
    Comentario       NVARCHAR(1000) NULL,
    FechaAccion      DATETIME2(0) NOT NULL
        CONSTRAINT DF_Historial_Fecha DEFAULT (SYSDATETIME()),

    CONSTRAINT PK_Historial PRIMARY KEY (HistorialId),
    CONSTRAINT FK_Historial_Incidencia
        FOREIGN KEY (IncidenciaId) REFERENCES dbo.Incidencia (IncidenciaId),
    CONSTRAINT FK_Historial_EstadoOrigen
        FOREIGN KEY (EstadoOrigenId) REFERENCES dbo.EstadoIncidencia (EstadoId),
    CONSTRAINT FK_Historial_EstadoDestino
        FOREIGN KEY (EstadoDestinoId) REFERENCES dbo.EstadoIncidencia (EstadoId),
    CONSTRAINT FK_Historial_Empleado
        FOREIGN KEY (EmpleadoAccionId) REFERENCES dbo.Empleado (EmpleadoId),
    CONSTRAINT CK_Historial_Accion
        CHECK (Accion IN ('REGISTRO', 'ASIGNACION', 'INICIO_ATENCION', 'RESOLUCION'))
);
GO

--verificacion final
SELECT COUNT(*) AS Tablas         FROM sys.tables;
SELECT COUNT(*) AS LlavesForaneas FROM sys.foreign_keys;
SELECT COUNT(*) AS Checks         FROM sys.check_constraints;
SELECT COUNT(*) AS Unicos         FROM sys.key_constraints WHERE type = 'UQ';
GO