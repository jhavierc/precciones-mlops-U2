package com.mlops.pacientes.dto;

import java.util.List;

public record Response (
        String primerNombre,
        String segundoNombre,
        String primerApellido,
        String segundoApellido,
        String genero,
        Integer edad,
        List<String> habitos,
        String prediccion
){
}
