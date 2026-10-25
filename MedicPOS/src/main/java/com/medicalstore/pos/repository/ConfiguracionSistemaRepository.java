package com.medicalstore.pos.repository;

import com.medicalstore.pos.entity.ConfiguracionSistema;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConfiguracionSistemaRepository extends JpaRepository<ConfiguracionSistema, Long> {
    Optional<ConfiguracionSistema> findByClave(String clave);

    List<ConfiguracionSistema> findByActivoTrueOrderByClaveAsc();

    List<ConfiguracionSistema> findAllByOrderByClaveAsc();
}
