package com.medicalstore.pos.repository;

import com.medicalstore.pos.entity.MovimientoInventario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MovimientoInventarioRepository extends JpaRepository<MovimientoInventario, Long> {
    @Query("SELECT m FROM MovimientoInventario m JOIN FETCH m.medicamento JOIN FETCH m.lote " +
            "JOIN FETCH m.usuario u JOIN FETCH u.rol ORDER BY m.fechaHora DESC")
    List<MovimientoInventario> findAllDetallado();

    @Query("SELECT m FROM MovimientoInventario m JOIN FETCH m.medicamento JOIN FETCH m.lote " +
            "JOIN FETCH m.usuario u JOIN FETCH u.rol " +
            "WHERE m.lote.id = :loteId ORDER BY m.fechaHora DESC")
    List<MovimientoInventario> findByLoteId(@Param("loteId") Long loteId);

    @Query("SELECT m FROM MovimientoInventario m JOIN FETCH m.medicamento JOIN FETCH m.lote " +
            "JOIN FETCH m.usuario u JOIN FETCH u.rol " +
            "WHERE m.referenciaTipo = :tipo AND m.referenciaId = :id")
    List<MovimientoInventario> findByReferencia(@Param("tipo") String tipo, @Param("id") Long id);
}
