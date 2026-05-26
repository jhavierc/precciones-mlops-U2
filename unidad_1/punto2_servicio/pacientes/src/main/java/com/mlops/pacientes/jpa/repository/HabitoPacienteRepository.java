package com.mlops.pacientes.jpa.repository;

import com.mlops.pacientes.jpa.entity.HabitoPacienteEntity;
import com.mlops.pacientes.jpa.entity.PacienteEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HabitoPacienteRepository extends JpaRepository<HabitoPacienteEntity, Long> {

    @Query("SELECT hp.paciente.idPaciente FROM HabitoPacienteEntity hp WHERE hp.habito.nombreHabito IN :nombres")
    List<Long> findIdsPacientesByHabitos(@Param("nombres") List<String> nombres);

    List<HabitoPacienteEntity> findByPaciente(PacienteEntity paciente);

}
