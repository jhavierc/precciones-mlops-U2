package com.mlops.pacientes.service;

import com.mlops.pacientes.dto.Request;
import com.mlops.pacientes.dto.Response;
import com.mlops.pacientes.dto.ReportePredicciones;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;

public interface IPacienteService {

    List<Map<String, Integer>> predecir(@RequestBody @Validated Request request);
    List<Response> detallePrediccion(Request request) throws Throwable;
    ReportePredicciones reportePredicciones();

}
