package com.medicalstore.pos.repository;

import com.medicalstore.pos.entity.Cobro;
import com.medicalstore.pos.entity.EstadoCobro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CobroRepository extends JpaRepository<Cobro, Long> {

    Optional<Cobro> findByDispensacionId(Long dispensacionId);

    boolean existsByDispensacionId(Long dispensacionId);

    @Query("SELECT c FROM Cobro c WHERE c.estado = :estado")
    List<Cobro> findByEstado(@Param("estado") EstadoCobro estado);

    @Query("SELECT c FROM Cobro c WHERE c.createdAt BETWEEN :desde AND :hasta ORDER BY c.createdAt DESC")
    List<Cobro> findByRangoFechas(@Param("desde") LocalDateTime desde, @Param("hasta") LocalDateTime hasta);

    @Query("SELECT c FROM Cobro c WHERE c.dispensacion.receta.tipoReceta = :tipo ORDER BY c.createdAt DESC")
    List<Cobro> findByTipoReceta(@Param("tipo") String tipo);
}