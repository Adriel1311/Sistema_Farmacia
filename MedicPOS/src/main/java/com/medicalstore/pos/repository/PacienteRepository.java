package com.medicalstore.pos.repository;

import com.medicalstore.pos.entity.Paciente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PacienteRepository extends JpaRepository<Paciente, Long> {
    Optional<Paciente> findByDni(String dni);
    boolean existsByDni(String dni);

    List<Paciente> findByActivoTrueOrderByNombresAsc();

    @Query("SELECT p FROM Paciente p WHERE p.activo = true AND (p.dni ILIKE '%' || :texto || '%' " +
            "OR p.nombres ILIKE '%' || :texto || '%' OR p.apellidos ILIKE '%' || :texto || '%') " +
            "ORDER BY p.apellidos ASC, p.nombres ASC")
    List<Paciente> buscar(@Param("texto") String texto);
}
