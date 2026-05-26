package com.mlops.pacientes.jpa.repository;

import com.mlops.pacientes.jpa.entity.HabitoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HabitoRepository extends JpaRepository<HabitoEntity, Long> {

    HabitoEntity findByNombreHabito(String nombreHabito);

}
