package com.mlops.pacientes.config;

import com.mlops.pacientes.jpa.entity.HabitoEntity;
import com.mlops.pacientes.jpa.entity.HabitoPacienteEntity;
import com.mlops.pacientes.jpa.entity.PacienteEntity;
import com.mlops.pacientes.jpa.repository.HabitoPacienteRepository;
import com.mlops.pacientes.jpa.repository.HabitoRepository;
import com.mlops.pacientes.jpa.repository.PacienteRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class DataInitializer {

    private static final int TOTAL_PACIENTES = 563;

    private static final String[] PRIMEROS_NOMBRES_FEMENINOS = {
            "Maria", "Ana", "Laura", "Sofia", "Camila", "Valentina", "Isabella", "Paula", "Daniela", "Natalia"
    };

    private static final String[] SEGUNDOS_NOMBRES_FEMENINOS = {
            "Fernanda", "Patricia", "Carolina", "Alejandra", "Marcela", "Lucia", "Gabriela", "Elena", "Adriana", "Victoria"
    };

    private static final String[] PRIMEROS_NOMBRES_MASCULINOS = {
            "Carlos", "Juan", "Luis", "Pedro", "Andres", "Jorge", "Diego", "Fernando", "Ricardo", "Miguel"
    };

    private static final String[] SEGUNDOS_NOMBRES_MASCULINOS = {
            "Alberto", "David", "Enrique", "Felipe", "Sebastian", "Eduardo", "Rafael", "Antonio", "Mauricio", "Santiago"
    };

    private static final String[] PRIMEROS_APELLIDOS = {
            "Garcia", "Rodriguez", "Martinez", "Lopez", "Gonzalez", "Perez", "Sanchez", "Ramirez", "Torres", "Flores",
            "Rivera", "Gomez", "Diaz", "Vargas", "Morales", "Castro", "Ortiz", "Rojas", "Mendoza", "Herrera"
    };

    private static final String[] SEGUNDOS_APELLIDOS = {
            "Jimenez", "Moreno", "Alvarez", "Romero", "Ruiz", "Navarro", "Medina", "Cruz", "Reyes", "Silva",
            "Paredes", "Campos", "Vega", "Molina", "Aguilar", "Cortes", "Leon", "Guerrero", "Salazar", "Peña"
    };

    @Bean
    CommandLineRunner loadInitialData(
            PacienteRepository pacienteRepository,
            HabitoRepository habitoRepository,
            HabitoPacienteRepository habitoPacienteRepository
    ) {
        return args -> {
            if (pacienteRepository.count() > 0 || habitoRepository.count() > 0) {
                return;
            }

            List<HabitoEntity> habitos = habitoRepository.saveAll(crearHabitos());
            List<PacienteEntity> pacientes = pacienteRepository.saveAll(crearPacientes());
            habitoPacienteRepository.saveAll(crearRelaciones(pacientes, habitos));
        };
    }

    private List<HabitoEntity> crearHabitos() {
        List<HabitoEntity> habitos = new ArrayList<>();
        habitos.add(crearHabito("Actividad fisica regular", true));
        habitos.add(crearHabito("Alimentacion balanceada", true));
        habitos.add(crearHabito("Dormir entre 7 y 8 horas", true));
        habitos.add(crearHabito("Hidratacion frecuente", true));
        habitos.add(crearHabito("Chequeos medicos preventivos", true));
        habitos.add(crearHabito("Consumo de tabaco", false));
        habitos.add(crearHabito("Consumo frecuente de alcohol", false));
        habitos.add(crearHabito("Sedentarismo", false));
        habitos.add(crearHabito("Alta ingesta de azucar", false));
        habitos.add(crearHabito("Mala higiene del sueño", false));
        return habitos;
    }

    private HabitoEntity crearHabito(String nombre, boolean bueno) {
        HabitoEntity habito = new HabitoEntity();
        habito.setNombreHabito(nombre);
        habito.setBueno(bueno);
        return habito;
    }

    private List<PacienteEntity> crearPacientes() {
        List<PacienteEntity> pacientes = new ArrayList<>();
        for (int i = 0; i < TOTAL_PACIENTES; i++) {
            boolean esFemenino = i % 2 == 0;
            PacienteEntity paciente = new PacienteEntity();
            paciente.setPrimerNombre(esFemenino
                    ? PRIMEROS_NOMBRES_FEMENINOS[i % PRIMEROS_NOMBRES_FEMENINOS.length]
                    : PRIMEROS_NOMBRES_MASCULINOS[i % PRIMEROS_NOMBRES_MASCULINOS.length]);
            paciente.setSegundoNombre(esFemenino
                    ? SEGUNDOS_NOMBRES_FEMENINOS[(i * 3) % SEGUNDOS_NOMBRES_FEMENINOS.length]
                    : SEGUNDOS_NOMBRES_MASCULINOS[(i * 3) % SEGUNDOS_NOMBRES_MASCULINOS.length]);
            paciente.setPrimerApellido(PRIMEROS_APELLIDOS[(i * 5) % PRIMEROS_APELLIDOS.length]);
            paciente.setSegundoApellido(SEGUNDOS_APELLIDOS[(i * 7) % SEGUNDOS_APELLIDOS.length]);
            paciente.setEdad(18 + (i * 4) % 73);
            paciente.setGenero(esFemenino ? "Femenino" : "Masculino");
            pacientes.add(paciente);
        }
        return pacientes;
    }

    private List<HabitoPacienteEntity> crearRelaciones(List<PacienteEntity> pacientes, List<HabitoEntity> habitos) {
        List<HabitoPacienteEntity> relaciones = new ArrayList<>();
        for (int i = 0; i < pacientes.size(); i++) {
            PacienteEntity paciente = pacientes.get(i);
            int perfilHabitos = i % 5;
            if (perfilHabitos == 0) {
                relaciones.add(crearRelacion(paciente, habitos.get(i % 5)));
                relaciones.add(crearRelacion(paciente, habitos.get((i + 2) % 5)));
                relaciones.add(crearRelacion(paciente, habitos.get((i + 4) % 5)));
            } else if (perfilHabitos == 1) {
                relaciones.add(crearRelacion(paciente, habitos.get(i % 5)));
                relaciones.add(crearRelacion(paciente, habitos.get((i + 2) % 5)));
                relaciones.add(crearRelacion(paciente, habitos.get(5 + (i + 4) % 5)));
            } else if (perfilHabitos == 2) {
                relaciones.add(crearRelacion(paciente, habitos.get(i % 5)));
                relaciones.add(crearRelacion(paciente, habitos.get(5 + (i + 2) % 5)));
                relaciones.add(crearRelacion(paciente, habitos.get(5 + (i + 4) % 5)));
            } else if (perfilHabitos == 3) {
                relaciones.add(crearRelacion(paciente, habitos.get(5 + i % 5)));
                relaciones.add(crearRelacion(paciente, habitos.get(5 + (i + 2) % 5)));
                relaciones.add(crearRelacion(paciente, habitos.get(5 + (i + 4) % 5)));
            } else {
                relaciones.add(crearRelacion(paciente, habitos.get(5 + i % 5)));
                relaciones.add(crearRelacion(paciente, habitos.get(5 + (i + 2) % 5)));
                relaciones.add(crearRelacion(paciente, habitos.get(5 + (i + 4) % 5)));
            }
        }
        return relaciones;
    }

    private HabitoPacienteEntity crearRelacion(PacienteEntity paciente, HabitoEntity habito) {
        HabitoPacienteEntity relacion = new HabitoPacienteEntity();
        relacion.setPaciente(paciente);
        relacion.setHabito(habito);
        return relacion;
    }
}
