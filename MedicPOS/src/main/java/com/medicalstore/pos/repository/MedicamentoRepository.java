package com.medicalstore.pos.repository;

import com.medicalstore.pos.entity.Medicamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MedicamentoRepository extends JpaRepository<Medicamento, Long> {
    Optional<Medicamento> findByCodigo(String codigo);
    boolean existsByCodigo(String codigo);

    @Query("SELECT m FROM Medicamento m JOIN FETCH m.proveedor WHERE m.activo = true ORDER BY m.nombre ASC")
    List<Medicamento> findAllActivosWithProveedor();

    @Query("SELECT m FROM Medicamento m JOIN FETCH m.proveedor WHERE m.id = :id")
    Optional<Medicamento> findByIdWithProveedor(@Param("id") Long id);

    @Query("SELECT m FROM Medicamento m JOIN FETCH m.proveedor " +
            "WHERE m.activo = true AND (m.nombre ILIKE '%' || :texto || '%' " +
            "OR m.codigo ILIKE '%' || :texto || '%') " +
            "ORDER BY m.nombre ASC")
    List<Medicamento> buscarActivos(@Param("texto") String texto);

    @Query("SELECT m FROM Medicamento m JOIN FETCH m.proveedor WHERE m.proveedor.id = :proveedorId AND m.activo = true " +
            "ORDER BY m.nombre ASC")
    List<Medicamento> findByProveedorIdAndActivoTrue(@Param("proveedorId") Long proveedorId);

    long countByActivoTrue();
}
