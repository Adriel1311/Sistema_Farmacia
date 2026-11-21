package com.medicalstore.pos.repository;

import com.medicalstore.pos.entity.MovimientoCaja;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface MovimientoCajaRepository extends JpaRepository<MovimientoCaja, Long> {

    @Query("SELECT m FROM MovimientoCaja m JOIN FETCH m.usuario " +
            "LEFT JOIN FETCH m.dispensacion d LEFT JOIN FETCH d.receta r LEFT JOIN FETCH r.paciente " +
            "WHERE m.caja.id = :cajaId ORDER BY m.fechaHora DESC, m.id DESC")
    List<MovimientoCaja> findByCajaDetallado(@Param("cajaId") Long cajaId);

    /** Filas [tipo, suma de montos, cantidad de movimientos] de una caja. */
    @Query("SELECT m.tipo, COALESCE(SUM(m.monto), 0), COUNT(m) FROM MovimientoCaja m " +
            "WHERE m.caja.id = :cajaId GROUP BY m.tipo")
    List<Object[]> totalesPorTipo(@Param("cajaId") Long cajaId);

    /** Ventas (con su detalle) de un conjunto de boletas. */
    @Query("SELECT DISTINCT m FROM MovimientoCaja m LEFT JOIN FETCH m.items " +
            "WHERE m.tipo = :tipo AND m.dispensacion.id IN :ids")
    List<MovimientoCaja> findConItemsByTipoAndDispensacionIds(@Param("tipo") MovimientoCaja.Tipo tipo,
                                                              @Param("ids") Collection<Long> ids);

    @Query("SELECT m FROM MovimientoCaja m WHERE m.tipo = :tipo AND m.dispensacion.id = :dispensacionId")
    Optional<MovimientoCaja> findByTipoAndDispensacionId(@Param("tipo") MovimientoCaja.Tipo tipo,
                                                         @Param("dispensacionId") Long dispensacionId);

    boolean existsByDispensacionIdAndTipo(Long dispensacionId, MovimientoCaja.Tipo tipo);
}
