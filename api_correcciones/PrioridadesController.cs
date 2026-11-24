using GestionIncidencias.Api.Dtos;
using GestionIncidencias.Api.Repositories;
using Microsoft.AspNetCore.Mvc;

namespace GestionIncidencias.Api.Controllers;

[ApiController]
[Route("api/prioridades")]
public class PrioridadesController : ControllerBase
{
    private readonly CatalogosRepository _repository;
    private readonly ILogger<PrioridadesController> _logger;

    public PrioridadesController(
        CatalogosRepository repository,
        ILogger<PrioridadesController> logger)
    {
        _repository = repository;
        _logger = logger;
    }

    [HttpGet]
    public async Task<IActionResult> Get()
    {
        try
        {
            var prioridades = await _repository.ObtenerPrioridadesAsync();

            return Ok(new ApiResponse<List<CatalogoDto>>
            {
                Success = true,
                Message = "Prioridades obtenidas correctamente",
                Data = prioridades
            });
        }
        catch (Exception ex)
        {
            _logger.LogError(
                ex,
                "Error al consultar el catálogo de prioridades.");

            return StatusCode(
                StatusCodes.Status500InternalServerError,
                new ApiResponse<object>
                {
                    Success = false,
                    Message = "Ocurrió un error al consultar las prioridades",
                    Data = null
                });
        }
    }
}
