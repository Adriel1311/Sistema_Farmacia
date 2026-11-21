package com.medicalstore.pos.repository;

import com.medicalstore.pos.entity.Medico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MedicoRepository extends JpaRepository<Medico, Long> {

    Optional<Medico> findByNombre(String nombre);

    boolean existsByNombre(String nombre);

    @Query("SELECT m FROM Medico m WHERE m.activo = true ORDER BY m.nombre ASC")
    List<Medico> findAllActivos();

    @Query("SELECT m FROM Medico m WHERE m.activo = true AND m.nombre LIKE %:texto% ORDER BY m.nombre ASC")
    List<Medico> buscarActivos(@Param("texto") String texto);
}