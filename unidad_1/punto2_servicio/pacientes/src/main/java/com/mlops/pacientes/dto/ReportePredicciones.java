package com.mlops.pacientes.dto;

import java.util.List;
import java.util.Map;

public record ReportePredicciones(
        Map<String, Integer> totalPorCategoria,
        List<PrediccionRealizada> ultimasPredicciones,
        String fechaUltimaPrediccion
) {
}
