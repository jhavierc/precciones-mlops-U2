package com.mlops.pacientes.service;

import com.mlops.pacientes.dto.PrediccionRealizada;
import com.mlops.pacientes.dto.ReportePredicciones;
import com.mlops.pacientes.dto.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class RegistroPrediccionesService {

    public static final List<String> CATEGORIAS = List.of(
            "NO ENFERMO",
            "ENFERMEDAD LEVE",
            "ENFERMEDAD AGUDA",
            "ENFERMEDAD CRÓNICA",
            "ENFERMEDAD TERMINAL"
    );

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private final Path rutaReporte;

    public RegistroPrediccionesService(
            @Value("${pacientes.reporte.path:/tmp/pacientes/predicciones.log}") String rutaReporte
    ) {
        this.rutaReporte = Path.of(rutaReporte);
    }

    public synchronized void registrar(Response response) {
        try {
            Path parent = rutaReporte.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            String linea = String.join("|",
                    FORMATO_FECHA.format(LocalDateTime.now()),
                    nombreCompleto(response),
                    response.genero(),
                    String.valueOf(response.edad()),
                    response.prediccion()
            );
            Files.writeString(
                    rutaReporte,
                    linea + System.lineSeparator(),
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND
            );
        } catch (IOException e) {
            throw new IllegalStateException("No fue posible registrar la prediccion en el archivo de reporte.", e);
        }
    }

    public synchronized ReportePredicciones generarReporte() {
        List<PrediccionRealizada> predicciones = leerPredicciones();
        Map<String, Integer> totalPorCategoria = inicializarConteo();

        predicciones.forEach(prediccion ->
                totalPorCategoria.merge(prediccion.prediccion(), 1, Integer::sum));

        int desde = Math.max(predicciones.size() - 5, 0);
        List<PrediccionRealizada> ultimasPredicciones = predicciones.subList(desde, predicciones.size());
        String fechaUltimaPrediccion = predicciones.isEmpty()
                ? null
                : predicciones.get(predicciones.size() - 1).fecha();

        return new ReportePredicciones(totalPorCategoria, ultimasPredicciones, fechaUltimaPrediccion);
    }

    private List<PrediccionRealizada> leerPredicciones() {
        if (!Files.exists(rutaReporte)) {
            return List.of();
        }

        try {
            return Files.readAllLines(rutaReporte, StandardCharsets.UTF_8).stream()
                    .map(this::parsearLinea)
                    .toList();
        } catch (IOException e) {
            throw new IllegalStateException("No fue posible leer el archivo de reporte de predicciones.", e);
        }
    }

    private PrediccionRealizada parsearLinea(String linea) {
        String[] campos = linea.split("\\|", -1);
        if (campos.length < 5) {
            return new PrediccionRealizada("", "", "", null, "DESCONOCIDA");
        }
        return new PrediccionRealizada(
                campos[0],
                campos[1],
                campos[2],
                Integer.valueOf(campos[3]),
                campos[4]
        );
    }

    private Map<String, Integer> inicializarConteo() {
        Map<String, Integer> totalPorCategoria = new LinkedHashMap<>();
        CATEGORIAS.forEach(categoria -> totalPorCategoria.put(categoria, 0));
        return totalPorCategoria;
    }

    private String nombreCompleto(Response response) {
        List<String> nombres = new ArrayList<>();
        nombres.add(response.primerNombre());
        nombres.add(response.segundoNombre());
        nombres.add(response.primerApellido());
        nombres.add(response.segundoApellido());
        return String.join(" ", nombres.stream()
                .filter(nombre -> nombre != null && !nombre.isBlank())
                .toList());
    }
}
