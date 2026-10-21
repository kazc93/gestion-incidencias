--06 consultas y reportes
use incidencias;
GO

--total de incidencias
select count(*) as Total_Incidencias from dbo.Incidencia
where Activo = 1

--cantidad de incidencias por estado
select es.Nombre as Estado, count(i.IncidenciaId) as Cantidad
from dbo.EstadoIncidencia es
left join dbo.Incidencia i
  on es.EstadoId = i.EstadoId
  and i.Activo = 1
group by es.Orden, es.Nombre
order by es.Orden

--cantidad de incidencias por tipo
select t.Nombre as Tipo, count(i.IncidenciaId) as Cantidad
from dbo.TipoIncidencia t
left join dbo.Incidencia i
  on t.TipoId = i.TipoId
  and i.Activo = 1
group by t.TipoId, t.Nombre
order by Cantidad desc

--cantidad de incidencias por prioridad
select p.Nombre as Prioridad, count(i.IncidenciaId) as Cantidad
from dbo.Prioridad p
left join dbo.Incidencia i
  on p.PrioridadId = i.PrioridadId
  and i.Activo = 1
group by p.Nivel, p.Nombre
order by p.Nivel desc

--incidencias por responsable
select concat(e.Nombre, ' ', e.Apellidos) as Responsable, count(a.IncidenciaId) as Cantidad
from dbo.Empleado e
inner join dbo.Asignacion a
  on e.EmpleadoId = a.ResponsableEmpleadoId
where a.Activa = 1
group by e.EmpleadoId, e.Nombre, e.Apellidos
order by Cantidad desc

--incidencias reportadas por empleado
select concat(e.Nombre, ' ', e.Apellidos) as Empleado, count(i.IncidenciaId) as Cantidad
from dbo.Empleado e
inner join dbo.Incidencia i
  on e.EmpleadoId = i.ReportaEmpleadoId
group by e.EmpleadoId, e.Nombre, e.Apellidos
order by Cantidad desc

--incidencias pendientes
select * from dbo.vw_incidencias
where Estado = 'Pendiente'

--incidencias sin responsable
select * from dbo.vw_incidencias
where Responsable is null

--incidencias por rango de fechas
select * from dbo.vw_incidencias
where FechaRegistro between '2026-01-01' and '2026-12-31'

--incidencias resueltas con su solucion
select i.Folio, i.Titulo, i.Solucion, i.FechaResolucion
from dbo.Incidencia i
inner join dbo.EstadoIncidencia es
  on i.EstadoId = es.EstadoId
where es.Clave = 'RESUELTA'

--historial de una incidencia
select h.Accion, eo.Nombre as Estado_Origen, ed.Nombre as Estado_Destino,
  concat(e.Nombre, ' ', e.Apellidos) as Hecho_Por, h.FechaAccion
from dbo.HistorialIncidencia h
left join dbo.EstadoIncidencia eo
  on h.EstadoOrigenId = eo.EstadoId
inner join dbo.EstadoIncidencia ed
  on h.EstadoDestinoId = ed.EstadoId
inner join dbo.Empleado e
  on h.EmpleadoAccionId = e.EmpleadoId
where h.IncidenciaId = 1
order by h.HistorialId

--fotografias de una incidencia
select f.TipoEvidencia, f.NombreArchivo, f.UrlAlmacenamiento, f.FechaCaptura
from dbo.Fotografia f
where f.IncidenciaId = 1