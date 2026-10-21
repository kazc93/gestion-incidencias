--05 datos de prueba
use incidencias;
GO

--registrar incidencias
EXEC sp_registrar_incidencia
 @titulo = N'Lámpara quemada', @descripcion = N'No enciende la lámpara del pasillo principal',
 @idTipo = 5, @idPrioridad = 2, @idReporta = 1,
 @urlFoto = '/fotos/inc1_inicial.jpg', @nombreArchivo = 'inc1_inicial.jpg';
GO

EXEC sp_registrar_incidencia
 @titulo = N'Internet lento en oficina', @descripcion = N'La conexión se cae varias veces al día',
 @idTipo = 1, @idPrioridad = 3, @idReporta = 2,
 @urlFoto = '/fotos/inc2_inicial.jpg', @nombreArchivo = 'inc2_inicial.jpg';
GO

EXEC sp_registrar_incidencia
 @titulo = N'Computadora no enciende', @descripcion = N'La computadora de recepción no da imagen ni enciende',
 @idTipo = 2, @idPrioridad = 4, @idReporta = 4,
 @urlFoto = '/fotos/inc3_inicial.jpg', @nombreArchivo = 'inc3_inicial.jpg';
GO

EXEC sp_registrar_incidencia
 @titulo = N'Silla rota', @descripcion = N'La silla de la sala de reuniones tiene una pata quebrada',
 @idTipo = 4, @idPrioridad = 1, @idReporta = 5,
 @urlFoto = '/fotos/inc4_inicial.jpg', @nombreArchivo = 'inc4_inicial.jpg';
GO

EXEC sp_registrar_incidencia
 @titulo = N'Gotera en el techo', @descripcion = N'Cae agua del techo cuando llueve en el área de archivo',
 @idTipo = 3, @idPrioridad = 3, @idReporta = 6,
 @urlFoto = '/fotos/inc5_inicial.jpg', @nombreArchivo = 'inc5_inicial.jpg';
GO

EXEC sp_registrar_incidencia
 @titulo = N'Piso sucio en pasillo', @descripcion = N'Hay manchas y basura en el pasillo del segundo nivel',
 @idTipo = 6, @idPrioridad = 1, @idReporta = 3,
 @urlFoto = '/fotos/inc6_inicial.jpg', @nombreArchivo = 'inc6_inicial.jpg';
GO

EXEC sp_registrar_incidencia
 @titulo = N'Impresora sin conexión', @descripcion = N'La impresora de contabilidad no aparece en la red',
 @idTipo = 2, @idPrioridad = 2, @idReporta = 1,
 @urlFoto = '/fotos/inc7_inicial.jpg', @nombreArchivo = 'inc7_inicial.jpg';
GO

EXEC sp_registrar_incidencia
 @titulo = N'Puerta no cierra bien', @descripcion = N'La puerta de la bodega no cierra con llave',
 @idTipo = 3, @idPrioridad = 2, @idReporta = 4,
 @urlFoto = '/fotos/inc8_inicial.jpg', @nombreArchivo = 'inc8_inicial.jpg';
GO

--asignar incidencias 1 al 6
EXEC sp_asignar_incidencia @idIncidencia = 1, @idResponsable = 3, @idAsigna = 2, @comentario = N'Asignada a Luis';
GO
EXEC sp_asignar_incidencia @idIncidencia = 2, @idResponsable = 4, @idAsigna = 2, @comentario = N'Asignada a María';
GO
EXEC sp_asignar_incidencia @idIncidencia = 3, @idResponsable = 5, @idAsigna = 2, @comentario = N'Asignada a Pedro';
GO
EXEC sp_asignar_incidencia @idIncidencia = 4, @idResponsable = 6, @idAsigna = 2, @comentario = N'Asignada a Sofía';
GO
EXEC sp_asignar_incidencia @idIncidencia = 5, @idResponsable = 3, @idAsigna = 2, @comentario = N'Asignada a Luis';
GO
EXEC sp_asignar_incidencia @idIncidencia = 6, @idResponsable = 4, @idAsigna = 2, @comentario = N'Asignada a María';
GO

--iniciar atencion incidencias 1 al 4
EXEC sp_iniciar_atencion @idIncidencia = 1, @idEmpleado = 3;
GO
EXEC sp_iniciar_atencion @idIncidencia = 2, @idEmpleado = 4;
GO
EXEC sp_iniciar_atencion @idIncidencia = 3, @idEmpleado = 5;
GO
EXEC sp_iniciar_atencion @idIncidencia = 4, @idEmpleado = 6;
GO

--resolver incidencias 1 y 2
EXEC sp_resolver_incidencia
 @idIncidencia = 1, @idEmpleado = 3, @solucion = N'Se cambió la lámpara por una nueva',
 @urlFoto = '/fotos/inc1_final.jpg', @nombreArchivo = 'inc1_final.jpg';
GO

EXEC sp_resolver_incidencia
 @idIncidencia = 2, @idEmpleado = 4, @solucion = N'Se reinició y se reconfiguró el router',
 @urlFoto = '/fotos/inc2_final.jpg', @nombreArchivo = 'inc2_final.jpg';
GO

--verificar
SELECT * FROM dbo.vw_incidencias;
GO