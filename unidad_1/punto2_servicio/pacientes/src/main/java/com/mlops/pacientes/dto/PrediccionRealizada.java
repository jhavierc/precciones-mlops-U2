package com.mlops.pacientes.dto;

public record PrediccionRealizada(
        String fecha,
        String paciente,
        String genero,
        Integer edad,
        String prediccion
) {
}
