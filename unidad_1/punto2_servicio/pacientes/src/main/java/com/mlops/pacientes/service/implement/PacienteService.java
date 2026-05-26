package com.mlops.pacientes.service.implement;

import com.mlops.pacientes.dto.Request;
import com.mlops.pacientes.dto.Response;
import com.mlops.pacientes.dto.ReportePredicciones;
import com.mlops.pacientes.jpa.entity.PacienteEntity;
import com.mlops.pacientes.jpa.repository.HabitoPacienteRepository;
import com.mlops.pacientes.jpa.repository.PacienteRepository;
import com.mlops.pacientes.service.IPacienteService;
import com.mlops.pacientes.service.RegistroPrediccionesService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@Service
@RequiredArgsConstructor
public class PacienteService implements IPacienteService {

    private final PacienteRepository pacienteRepository;
    private final HabitoPacienteRepository habitoPacienteRepository;
    private final RegistroPrediccionesService registroPrediccionesService;

    public List<Map<String, Integer>> predecir(Request request) {
        Map<String, Integer> conteoPorPrediccion = inicializarConteo();

        obtenerPacientesFiltrados(request).stream()
                .map(this::crearResponse)
                .forEach(response -> {
                    registroPrediccionesService.registrar(response);
                    conteoPorPrediccion.merge(response.prediccion(), 1, Integer::sum);
                });

        List<Map<String, Integer>> resultado = new ArrayList<>();
        conteoPorPrediccion.forEach((prediccion, cantidad) -> resultado.add(Map.of(prediccion, cantidad)));
        return resultado;
    }

    public List<Response> detallePrediccion(Request request) {
        return obtenerPacientesFiltrados(request).stream()
                .map(this::crearResponse)
                .peek(registroPrediccionesService::registrar)
                .toList();
    }

    public ReportePredicciones reportePredicciones() {
        return registroPrediccionesService.generarReporte();
    }

    private List<PacienteEntity> obtenerPacientesFiltrados(Request request) {
        List<PacienteEntity> pacientes = new ArrayList<>();
        List<PacienteEntity> pacientesPorGenero = pacienteRepository.findAll();
        Logger.getLogger("Intentando recuperar artículos para IDs: " + pacientesPorGenero.size());
        if (request.edad() != null && request.edad().size() >= 2) {
            pacientes = pacienteRepository.findByEdadBetween(request.edad().get(0), request.edad().get(1));
        }
        if (request.habitos() != null && !request.habitos().isEmpty()) {
            List<Long> idsPorHabitos = habitoPacienteRepository.findIdsPacientesByHabitos(request.habitos());

            if (pacientes.isEmpty() && (request.edad() == null || request.edad().isEmpty())) {
                pacientes = pacienteRepository.findAllById(idsPorHabitos);
            } else {
                pacientes = pacientes.stream()
                        .filter(p -> idsPorHabitos.contains(p.getIdPaciente()))
                        .toList();
            }
        }

        if (pacientes.isEmpty()
                && (request.edad() == null || request.edad().isEmpty())
                && (request.habitos() == null || request.habitos().isEmpty())) {
            pacientes = pacientesPorGenero;
        }

        return pacientes;
    }

    private List<String> obtenerNombresHabitos(PacienteEntity paciente) {
        return habitoPacienteRepository.findByPaciente(paciente).stream()
                .map(hp -> hp.getHabito().getNombreHabito())
                .toList();
    }

    private Response crearResponse(PacienteEntity paciente) {
        return new Response(
                paciente.getPrimerNombre(),
                paciente.getSegundoNombre(),
                paciente.getPrimerApellido(),
                paciente.getSegundoApellido(),
                paciente.getGenero(),
                paciente.getEdad(),
                obtenerNombresHabitos(paciente),
                generarPrediccion(paciente)
        );
    }

    private Map<String, Integer> inicializarConteo() {
        Map<String, Integer> conteoPorPrediccion = new LinkedHashMap<>();
        RegistroPrediccionesService.CATEGORIAS.forEach(categoria -> conteoPorPrediccion.put(categoria, 0));
        return conteoPorPrediccion;
    }

    private String generarPrediccion(PacienteEntity p) {
        int count = habitoPacienteRepository.findByPaciente(p).stream()
                .filter(hp -> !hp.getHabito().isBueno())
                .toList()
                .size();
        if (count == 0) return "NO ENFERMO";
        if (count == 1) return "ENFERMEDAD LEVE";
        if (count == 2) return "ENFERMEDAD AGUDA";
        if (count >= 3 && p.getEdad() >= 70) return "ENFERMEDAD TERMINAL";
        if (count == 3) return "ENFERMEDAD CRÓNICA";
        return "ENFERMEDAD TERMINAL";
    }
}
