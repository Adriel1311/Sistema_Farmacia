package com.medicalstore.pos.repository;

import com.medicalstore.pos.entity.BajaInventario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BajaInventarioRepository extends JpaRepository<BajaInventario, Long> {
    @Query("SELECT b FROM BajaInventario b JOIN FETCH b.lote l JOIN FETCH l.medicamento " +
            "JOIN FETCH b.usuario u JOIN FETCH u.rol ORDER BY b.fechaHora DESC")
    List<BajaInventario> findAllDetallado();

    @Query("SELECT b FROM BajaInventario b JOIN FETCH b.lote l JOIN FETCH l.medicamento " +
            "JOIN FETCH b.usuario u JOIN FETCH u.rol " +
            "WHERE b.fechaHora BETWEEN :desde AND :hasta " +
            "ORDER BY b.fechaHora DESC")
    List<BajaInventario> findByRangoFechas(@Param("desde") LocalDateTime desde, @Param("hasta") LocalDateTime hasta);

    @Query("SELECT b FROM BajaInventario b JOIN FETCH b.lote l JOIN FETCH l.medicamento " +
            "JOIN FETCH b.usuario u JOIN FETCH u.rol " +
            "WHERE b.motivo = :motivo ORDER BY b.fechaHora DESC")
    List<BajaInventario> findByMotivo(@Param("motivo") BajaInventario.Motivo motivo);

    long countByFechaHoraBetween(LocalDateTime desde, LocalDateTime hasta);
}
