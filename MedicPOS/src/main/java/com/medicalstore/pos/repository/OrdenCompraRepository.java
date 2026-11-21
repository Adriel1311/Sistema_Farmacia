package com.medicalstore.pos.repository;

import com.medicalstore.pos.entity.OrdenCompra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrdenCompraRepository extends JpaRepository<OrdenCompra, Long> {
    @Query("SELECT o FROM OrdenCompra o JOIN FETCH o.proveedor JOIN FETCH o.usuario u JOIN FETCH u.rol " +
            "ORDER BY o.fechaHora DESC")
    List<OrdenCompra> findAllDetallado();

    @Query("SELECT o FROM OrdenCompra o JOIN FETCH o.proveedor JOIN FETCH o.usuario u JOIN FETCH u.rol " +
            "WHERE o.id = :id")
    Optional<OrdenCompra> findByIdDetallado(@Param("id") Long id);

    // Se evita el patron "(:param IS NULL OR ...)": PostgreSQL no puede inferir el
    // tipo de un parametro null. El servicio elige la variante segun los filtros.
    @Query("SELECT o FROM OrdenCompra o JOIN FETCH o.proveedor JOIN FETCH o.usuario u JOIN FETCH u.rol " +
            "ORDER BY o.fechaHora DESC")
    List<OrdenCompra> findAllDetalladoOrderByFechaDesc();

    @Query("SELECT o FROM OrdenCompra o JOIN FETCH o.proveedor JOIN FETCH o.usuario u JOIN FETCH u.rol " +
            "WHERE o.estado = :estado ORDER BY o.fechaHora DESC")
    List<OrdenCompra> findByEstadoDetallado(@Param("estado") OrdenCompra.Estado estado);

    @Query("SELECT o FROM OrdenCompra o JOIN FETCH o.proveedor JOIN FETCH o.usuario u JOIN FETCH u.rol " +
            "WHERE o.proveedor.id = :proveedorId ORDER BY o.fechaHora DESC")
    List<OrdenCompra> findByProveedorIdDetallado(@Param("proveedorId") Long proveedorId);

    @Query("SELECT o FROM OrdenCompra o JOIN FETCH o.proveedor JOIN FETCH o.usuario u JOIN FETCH u.rol " +
            "WHERE o.estado = :estado AND o.proveedor.id = :proveedorId ORDER BY o.fechaHora DESC")
    List<OrdenCompra> findByEstadoAndProveedorIdDetallado(@Param("estado") OrdenCompra.Estado estado,
                                                         @Param("proveedorId") Long proveedorId);

    long countByEstado(OrdenCompra.Estado estado);

    boolean existsByProveedorIdAndEstadoIn(Long proveedorId, List<OrdenCompra.Estado> estados);
}
