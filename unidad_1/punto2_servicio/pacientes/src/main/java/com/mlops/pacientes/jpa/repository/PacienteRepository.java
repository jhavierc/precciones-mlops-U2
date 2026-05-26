package com.mlops.pacientes.jpa.repository;

import com.mlops.pacientes.jpa.entity.PacienteEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PacienteRepository extends JpaRepository<PacienteEntity, Long> {

    List<PacienteEntity> findByEdadBetween(Integer primeraEdad, Integer segundaEdad);

}
