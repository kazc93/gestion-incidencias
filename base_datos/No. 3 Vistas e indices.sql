--03 vistas e indices
use incidencias;
GO

--vista de incidencias
create view dbo.vw_incidencias
as
select
    i.IncidenciaId,
    i.Folio,
    i.Titulo,
    t.TipoId,
    t.Nombre as Tipo,
    p.Nombre as Prioridad,
    e.EstadoId,
    e.Nombre as Estado,
    i.FechaRegistro,
    concat(r.Nombre, ' ', r.Apellidos) as Reporta,
    concat(re.Nombre, ' ', re.Apellidos) as Responsable
from dbo.Incidencia i
inner join dbo.TipoIncidencia t
    on i.TipoId = t.TipoId
inner join dbo.Prioridad p
    on i.PrioridadId = p.PrioridadId
inner join dbo.EstadoIncidencia e
    on i.EstadoId = e.EstadoId
inner join dbo.Empleado r
    on i.ReportaEmpleadoId = r.EmpleadoId
left join dbo.Asignacion a
    on i.IncidenciaId = a.IncidenciaId
    and a.Activa = 1
left join dbo.Empleado re
    on a.ResponsableEmpleadoId = re.EmpleadoId
where i.Activo = 1;
GO

--indice filtro por estado
create index IX_Incidencia_Estado
    on dbo.Incidencia (EstadoId);
GO

--indice filtro por tipo
create index IX_Incidencia_Tipo
    on dbo.Incidencia (TipoId);
GO

--indice historial por incidencia
create index IX_Historial_Incidencia
    on dbo.HistorialIncidencia (IncidenciaId);
GO

--indice fotografias por incidencia
create index IX_Fotografia_Incidencia
    on dbo.Fotografia (IncidenciaId);
GO