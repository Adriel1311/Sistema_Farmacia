package com.medicalstore.pos.repository;

import com.medicalstore.pos.entity.Lote;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface LoteRepository extends JpaRepository<Lote, Long> {
    Optional<Lote> findByMedicamentoIdAndNumeroLote(Long medicamentoId, String numeroLote);

    boolean existsByMedicamentoIdAndNumeroLote(Long medicamentoId, String numeroLote);

    /**
     * Lotes vigentes de un medicamento ordenados por FEFO (RN02).
     * Solo lotes no vencidos, en estado ACTIVO y con existencias.
     */
    @Query("SELECT l FROM Lote l JOIN FETCH l.medicamento m JOIN FETCH m.proveedor " +
            "WHERE l.medicamento.id = :medicamentoId " +
            "AND l.estado = com.medicalstore.pos.entity.Lote$Estado.ACTIVO " +
            "AND l.cantidadActual > 0 " +
            "AND l.fechaVencimiento >= :hoy " +
            "ORDER BY l.fechaVencimiento ASC, l.id ASC")
    List<Lote> findVigentesFEFO(@Param("medicamentoId") Long medicamentoId, @Param("hoy") LocalDate hoy);

    /**
     * Igual que findVigentesFEFO pero con bloqueo pesimista de escritura.
     * Garantiza que dos dispensaciones simultaneas no descuenten el mismo lote (RN09).
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT l FROM Lote l WHERE l.medicamento.id = :medicamentoId " +
            "AND l.estado = com.medicalstore.pos.entity.Lote$Estado.ACTIVO " +
            "AND l.cantidadActual > 0 " +
            "AND l.fechaVencimiento >= :hoy " +
            "ORDER BY l.fechaVencimiento ASC, l.id ASC")
    List<Lote> findVigentesFEFOParaLockear(@Param("medicamentoId") Long medicamentoId, @Param("hoy") LocalDate hoy);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT l FROM Lote l WHERE l.id = :id")
    Optional<Lote> findByIdParaActualizar(@Param("id") Long id);

    /**
     * Stock vigente total: solo cuenta lo no vencido y no dado de baja (RN06).
     */
    @Query("SELECT COALESCE(SUM(l.cantidadActual), 0) FROM Lote l " +
            "WHERE l.medicamento.id = :medicamentoId " +
            "AND l.estado = com.medicalstore.pos.entity.Lote$Estado.ACTIVO " +
            "AND l.cantidadActual > 0 " +
            "AND l.fechaVencimiento >= :hoy")
    Integer getStockVigente(@Param("medicamentoId") Long medicamentoId, @Param("hoy") LocalDate hoy);

    /**
     * Stock vigente de varios medicamentos en una sola consulta (optimizacion de listados).
     */
    @Query("SELECT l.medicamento.id, COALESCE(SUM(l.cantidadActual), 0) FROM Lote l " +
            "WHERE l.medicamento.id IN :medicamentoIds " +
            "AND l.estado = com.medicalstore.pos.entity.Lote$Estado.ACTIVO " +
            "AND l.cantidadActual > 0 " +
            "AND l.fechaVencimiento >= :hoy " +
            "GROUP BY l.medicamento.id")
    List<Object[]> getStockVigentePorIds(@Param("medicamentoIds") List<Long> medicamentoIds, @Param("hoy") LocalDate hoy);

    /**
     * Lotes vencidos que aun tienen existencias fisicas en el estante (RN03).
     */
    @Query("SELECT l FROM Lote l JOIN FETCH l.medicamento m JOIN FETCH m.proveedor " +
            "WHERE l.estado = com.medicalstore.pos.entity.Lote$Estado.ACTIVO " +
            "AND l.cantidadActual > 0 " +
            "AND l.fechaVencimiento < :hoy " +
            "ORDER BY l.fechaVencimiento ASC")
    List<Lote> findVencidosConStock(@Param("hoy") LocalDate hoy);

    /**
     * Lotes que vencen dentro de la ventana de dias indicada (semaforo ROJO / AMBAR).
     */
    @Query("SELECT l FROM Lote l JOIN FETCH l.medicamento m JOIN FETCH m.proveedor " +
            "WHERE l.estado = com.medicalstore.pos.entity.Lote$Estado.ACTIVO " +
            "AND l.cantidadActual > 0 " +
            "AND l.fechaVencimiento >= :hoy " +
            "AND l.fechaVencimiento <= :limite " +
            "ORDER BY l.fechaVencimiento ASC")
    List<Lote> findVenciendo(@Param("hoy") LocalDate hoy, @Param("limite") LocalDate limite);

    @Query("SELECT l FROM Lote l JOIN FETCH l.medicamento m JOIN FETCH m.proveedor " +
            "ORDER BY l.fechaVencimiento ASC")
    List<Lote> findAllWithMedicamento();

    @Query("SELECT l FROM Lote l JOIN FETCH l.medicamento m JOIN FETCH m.proveedor " +
            "WHERE l.medicamento.id = :medicamentoId " +
            "ORDER BY l.fechaVencimiento ASC")
    List<Lote> findByMedicamentoIdWithMedicamento(@Param("medicamentoId") Long medicamentoId);

    // Nota: se evita el patron "(:param IS NULL OR ...)" porque PostgreSQL no puede
    // inferir el tipo del parametro cuando llega null. El servicio decide la variante.
    @Query("SELECT l FROM Lote l JOIN FETCH l.medicamento m JOIN FETCH m.proveedor " +
            "ORDER BY l.fechaVencimiento ASC")
    List<Lote> findAllWithMedicamentoOrderByVencimiento();

    @Query("SELECT l FROM Lote l JOIN FETCH l.medicamento m JOIN FETCH m.proveedor " +
            "WHERE m.id = :medicamentoId " +
            "ORDER BY l.fechaVencimiento ASC")
    List<Lote> findByMedicamentoIdOrdenado(@Param("medicamentoId") Long medicamentoId);

    @Query("SELECT l FROM Lote l JOIN FETCH l.medicamento m JOIN FETCH m.proveedor " +
            "WHERE l.estado = :estado " +
            "ORDER BY l.fechaVencimiento ASC")
    List<Lote> findByEstadoOrderByVencimiento(@Param("estado") Lote.Estado estado);

    @Query("SELECT l FROM Lote l JOIN FETCH l.medicamento m JOIN FETCH m.proveedor " +
            "WHERE m.id = :medicamentoId AND l.estado = :estado " +
            "ORDER BY l.fechaVencimiento ASC")
    List<Lote> findByMedicamentoIdAndEstadoOrderByVencimiento(@Param("medicamentoId") Long medicamentoId,
                                                              @Param("estado") Lote.Estado estado);

    long countByMedicamentoIdAndCantidadActualGreaterThan(Long medicamentoId, Integer cantidad);

    @Query("SELECT COALESCE(SUM(l.cantidadActual), 0) FROM Lote l " +
            "WHERE l.medicamento.id = :medicamentoId " +
            "AND l.estado = com.medicalstore.pos.entity.Lote$Estado.ACTIVO " +
            "AND l.cantidadActual > 0 " +
            "AND l.fechaVencimiento < :hoy")
    Integer getStockVencido(@Param("medicamentoId") Long medicamentoId, @Param("hoy") LocalDate hoy);

    @Query("SELECT l.medicamento.id, COALESCE(SUM(l.cantidadActual), 0) FROM Lote l " +
            "WHERE l.medicamento.id IN :medicamentoIds " +
            "AND l.estado = com.medicalstore.pos.entity.Lote$Estado.ACTIVO " +
            "AND l.cantidadActual > 0 " +
            "AND l.fechaVencimiento < :hoy " +
            "GROUP BY l.medicamento.id")
    List<Object[]> getStockVencidoPorIds(@Param("medicamentoIds") List<Long> medicamentoIds, @Param("hoy") LocalDate hoy);

    @Query("SELECT COUNT(l) FROM Lote l WHERE l.medicamento.id = :medicamentoId " +
            "AND l.estado = com.medicalstore.pos.entity.Lote$Estado.ACTIVO " +
            "AND l.cantidadActual > 0 AND l.fechaVencimiento < :hoy")
    long countLotesVencidos(@Param("medicamentoId") Long medicamentoId, @Param("hoy") LocalDate hoy);

    @Query("SELECT COUNT(l) FROM Lote l WHERE l.estado = com.medicalstore.pos.entity.Lote$Estado.BAJA")
    long countDadosDeBaja();

    @Query("SELECT COUNT(l) FROM Lote l WHERE l.estado = com.medicalstore.pos.entity.Lote$Estado.ACTIVO " +
            "AND l.cantidadActual > 0 AND l.fechaVencimiento < :hoy")
    long countVencidosConStock(@Param("hoy") LocalDate hoy);
}
