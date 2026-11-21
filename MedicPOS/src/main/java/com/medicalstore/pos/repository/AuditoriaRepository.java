package com.medicalstore.pos.repository;

import com.medicalstore.pos.entity.Auditoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditoriaRepository extends JpaRepository<Auditoria, Long> {
    List<Auditoria> findByEntidadOrderByFechaHoraDesc(String entidad);

    List<Auditoria> findByUsuarioIdOrderByFechaHoraDesc(Long usuarioId);

    @Query("SELECT a FROM Auditoria a JOIN FETCH a.usuario WHERE a.entidad = :entidad AND a.accion = :accion " +
            "ORDER BY a.fechaHora DESC")
    List<Auditoria> findByEntidadAndAccion(@Param("entidad") String entidad, @Param("accion") String accion);

    @Query("SELECT a FROM Auditoria a JOIN FETCH a.usuario WHERE a.fechaHora BETWEEN :desde AND :hasta " +
            "ORDER BY a.fechaHora DESC")
    List<Auditoria> findByRangoFechas(@Param("desde") LocalDateTime desde, @Param("hasta") LocalDateTime hasta);

    @Query("SELECT a FROM Auditoria a JOIN FETCH a.usuario ORDER BY a.fechaHora DESC")
    List<Auditoria> findAllWithUsuario();
}
