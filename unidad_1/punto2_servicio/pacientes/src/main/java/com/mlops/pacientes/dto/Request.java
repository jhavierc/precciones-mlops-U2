package com.mlops.pacientes.dto;

import java.util.List;

public record Request(
        List<Integer> edad,
        List<String> habitos
) {
}
