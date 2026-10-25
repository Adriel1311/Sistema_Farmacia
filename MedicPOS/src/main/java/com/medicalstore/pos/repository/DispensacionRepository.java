package com.medicalstore.pos.repository;

import com.medicalstore.pos.entity.Dispensacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface DispensacionRepository extends JpaRepository<Dispensacion, Long> {
    boolean existsByRecetaId(Long recetaId);

    Optional<Dispensacion> findByRecetaId(Long recetaId);

    @Query("SELECT d FROM Dispensacion d JOIN FETCH d.receta r JOIN FETCH r.paciente JOIN FETCH d.usuario u JOIN FETCH u.rol " +
            "ORDER BY d.fechaHora DESC")
    List<Dispensacion> findAllDetallado();

    @Query("SELECT d FROM Dispensacion d JOIN FETCH d.receta r JOIN FETCH r.paciente JOIN FETCH d.usuario u JOIN FETCH u.rol " +
            "WHERE d.id = :id")
    Optional<Dispensacion> findByIdDetallado(@Param("id") Long id);

    @Query("SELECT d FROM Dispensacion d JOIN FETCH d.receta r JOIN FETCH r.paciente JOIN FETCH d.usuario u JOIN FETCH u.rol " +
            "WHERE d.fechaHora BETWEEN :desde AND :hasta " +
            "ORDER BY d.fechaHora DESC")
    List<Dispensacion> findByRangoFechas(@Param("desde") LocalDateTime desde, @Param("hasta") LocalDateTime hasta);

    @Query("SELECT COUNT(d) FROM Dispensacion d WHERE d.fechaHora >= :desde AND d.estado = com.medicalstore.pos.entity.Dispensacion$Estado.COMPLETADA")
    long countCompletadasDesde(@Param("desde") LocalDateTime desde);

    @Query("SELECT d FROM Dispensacion d JOIN FETCH d.receta WHERE d.receta.id = :recetaId")
    Optional<Dispensacion> findByRecetaIdConReceta(@Param("recetaId") Long recetaId);
}
