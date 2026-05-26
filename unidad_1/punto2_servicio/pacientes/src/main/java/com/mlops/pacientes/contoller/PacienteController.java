package com.mlops.pacientes.contoller;

import com.mlops.pacientes.dto.Request;
import com.mlops.pacientes.dto.Response;
import com.mlops.pacientes.dto.ReportePredicciones;
import com.mlops.pacientes.service.IPacienteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/predecir")
@RequiredArgsConstructor
@Tag(
        name = "Predicción de pacientes",
        description = "Operaciones para consultar el nivel estimado de enfermedad de pacientes segun edad y habitos."
)
public class PacienteController {

    private final IPacienteService iPacienteService;

    @PostMapping("detalle")
    @Operation(
            summary = "Consultar detalle de predicción por paciente",
            description = "Retorna los pacientes que cumplen los filtros enviados junto con sus habitos y su prediccion individual."
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Filtros opcionales por rango de edad y nombres de habitos.",
            required = true,
            content = @Content(schema = @Schema(implementation = Request.class))
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Detalle de predicción generado correctamente.",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = Response.class)))
            ),
            @ApiResponse(responseCode = "400", description = "Solicitud invalida.", content = @Content)
    })
    public List<Response> detallePrediccion(@RequestBody @Validated Request request) throws Throwable{
        return iPacienteService.detallePrediccion(request);
    }

    @PostMapping
    @Operation(
            summary = "Consultar resumen de predicciones",
            description = "Retorna la cantidad de pacientes por nivel de enfermedad para los filtros enviados."
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Filtros opcionales por rango de edad y nombres de habitos.",
            required = true,
            content = @Content(schema = @Schema(implementation = Request.class))
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Resumen de predicciones generado correctamente.",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = Map.class)))
            ),
            @ApiResponse(responseCode = "400", description = "Solicitud invalida.", content = @Content)
    })
    public List<Map<String, Integer>> predecir(@RequestBody @Validated Request request){
        return iPacienteService.predecir(request);
    }

    @GetMapping("reporte")
    @Operation(
            summary = "Consultar reporte historico de predicciones",
            description = "Retorna totales por categoria, las ultimas 5 predicciones y la fecha de la ultima prediccion registrada."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Reporte de predicciones generado correctamente.",
                    content = @Content(schema = @Schema(implementation = ReportePredicciones.class))
            )
    })
    public ReportePredicciones reportePredicciones() {
        return iPacienteService.reportePredicciones();
    }
}
