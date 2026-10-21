--02 catalogos
use incidencias;
GO

--estados
INSERT INTO dbo.EstadoIncidencia (Clave, Nombre, Orden) VALUES
    ('PENDIENTE',   N'Pendiente',   1),
    ('ASIGNADA',    N'Asignada',    2),
    ('EN_ATENCION', N'En atención', 3),
    ('RESUELTA',    N'Resuelta',    4);
GO

--prioridades
INSERT INTO dbo.Prioridad (Clave, Nombre, Nivel) VALUES
    ('BAJA',    N'Baja',    1),
    ('MEDIA',   N'Media',   2),
    ('ALTA',    N'Alta',    3),
    ('CRITICA', N'Crítica', 4);
GO

--tipos de incidencia
INSERT INTO dbo.TipoIncidencia (Clave, Nombre) VALUES
    ('RED',          N'Red e internet'),
    ('EQUIPO',       N'Equipo de cómputo'),
    ('INFRA',        N'Infraestructura'),
    ('MOBILIARIO',   N'Mobiliario'),
    ('ELECTRICIDAD', N'Electricidad'),
    ('LIMPIEZA',     N'Limpieza'),
    ('OTRO',         N'Otro');
GO

--empleados
INSERT INTO dbo.Empleado (NumeroEmpleado, Nombre, Apellidos, Correo) VALUES
    ('EMP-001', N'Carlos',  N'Morales López',    'carlos.morales@ejemplo.com'),
    ('EMP-002', N'Ana',     N'Pérez García',     'ana.perez@ejemplo.com'),
    ('EMP-003', N'Luis',    N'Hernández Cruz',   'luis.hernandez@ejemplo.com'),
    ('EMP-004', N'María',   N'Gómez Ixcoy',      'maria.gomez@ejemplo.com'),
    ('EMP-005', N'Pedro',   N'Tzoc Batz',        'pedro.tzoc@ejemplo.com'),
    ('EMP-006', N'Sofía',   N'Ramírez Castillo', 'sofia.ramirez@ejemplo.com');
GO