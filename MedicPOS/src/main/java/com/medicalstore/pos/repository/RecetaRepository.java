package com.medicalstore.pos.repository;

import com.medicalstore.pos.entity.Receta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface RecetaRepository extends JpaRepository<Receta, Long> {
    Optional<Receta> findByNumeroReceta(String numeroReceta);
    boolean existsByNumeroReceta(String numeroReceta);

    @Query("SELECT r FROM Receta r JOIN FETCH r.paciente ORDER BY r.fechaReceta DESC, r.id DESC")
    List<Receta> findAllWithPaciente();

    @Query("SELECT r FROM Receta r JOIN FETCH r.paciente WHERE r.id = :id")
    Optional<Receta> findByIdWithPaciente(@Param("id") Long id);

    // Se evita el patron "(:param IS NULL OR ...)": PostgreSQL no puede inferir el
    // tipo de un parametro null. El servicio elige la variante segun los filtros.
    @Query("SELECT r FROM Receta r JOIN FETCH r.paciente ORDER BY r.fechaReceta DESC, r.id DESC")
    List<Receta> findAllWithPacienteOrderByFechaDesc();

    @Query("SELECT r FROM Receta r JOIN FETCH r.paciente WHERE r.tipoReceta = :tipo " +
            "ORDER BY r.fechaReceta DESC, r.id DESC")
    List<Receta> findByTipoWithPaciente(@Param("tipo") Receta.TipoReceta tipo);

    @Query("SELECT r FROM Receta r JOIN FETCH r.paciente " +
            "WHERE r.fechaReceta BETWEEN :desde AND :hasta ORDER BY r.fechaReceta DESC, r.id DESC")
    List<Receta> findByRangoFechasConPaciente(@Param("desde") LocalDate desde,
                                              @Param("hasta") LocalDate hasta);

    @Query("SELECT r FROM Receta r JOIN FETCH r.paciente " +
            "WHERE r.tipoReceta = :tipo AND r.fechaReceta BETWEEN :desde AND :hasta " +
            "ORDER BY r.fechaReceta DESC, r.id DESC")
    List<Receta> findByTipoAndRangoConPaciente(@Param("tipo") Receta.TipoReceta tipo,
                                               @Param("desde") LocalDate desde,
                                               @Param("hasta") LocalDate hasta);

    @Query("SELECT COUNT(r) FROM Receta r WHERE r.fechaReceta >= :desde")
    long countDesde(@Param("desde") LocalDate desde);

    boolean existsByNumeroRecetaAndIdNot(String numeroReceta, Long id);
}
