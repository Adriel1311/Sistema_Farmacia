package com.medicalstore.pos.repository;

import com.medicalstore.pos.entity.MedicamentoPrecio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface MedicamentoPrecioRepository extends JpaRepository<MedicamentoPrecio, Long> {

    @Query("""
        SELECT mp FROM MedicamentoPrecio mp
        WHERE mp.medicamento.id = :medicamentoId
          AND mp.vigenteDesde <= :fecha
          AND (mp.vigenteHasta IS NULL OR mp.vigenteHasta >= :fecha)
        ORDER BY mp.vigenteDesde DESC
        """)
    List<MedicamentoPrecio> findVigentesByMedicamentoYFecha(
            @Param("medicamentoId") Long medicamentoId,
            @Param("fecha") LocalDate fecha);

    Optional<MedicamentoPrecio> findFirstByMedicamentoIdOrderByVigenteDesdeDesc(Long medicamentoId);

    List<MedicamentoPrecio> findByMedicamentoIdOrderByVigenteDesdeDesc(Long medicamentoId);

    boolean existsByMedicamentoIdAndVigenteDesde(Long medicamentoId, LocalDate vigenteDesde);
}