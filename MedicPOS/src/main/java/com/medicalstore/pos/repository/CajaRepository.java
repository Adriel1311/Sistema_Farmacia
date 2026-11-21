package com.medicalstore.pos.repository;

import com.medicalstore.pos.entity.Caja;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CajaRepository extends JpaRepository<Caja, Long> {

    Optional<Caja> findByEstado(Caja.Estado estado);

    /**
     * Bloquea la caja abierta para que dos cobros o retiros simultaneos no
     * comprueben el mismo saldo. La unicidad de la caja abierta la garantiza la BD.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM Caja c WHERE c.estado = :estado")
    Optional<Caja> findByEstadoParaActualizar(@Param("estado") Caja.Estado estado);

    @Query("SELECT c FROM Caja c ORDER BY c.fechaApertura DESC")
    List<Caja> findAllRecientes();
}
