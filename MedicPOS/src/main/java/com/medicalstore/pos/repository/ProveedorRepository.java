package com.medicalstore.pos.repository;

import com.medicalstore.pos.entity.Proveedor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProveedorRepository extends JpaRepository<Proveedor, Long> {
    Optional<Proveedor> findByRuc(String ruc);
    boolean existsByRuc(String ruc);

    List<Proveedor> findByActivoTrueOrderByRazonSocialAsc();

    List<Proveedor> findByActivoTrueAndRazonSocialContainingIgnoreCaseOrderByRazonSocialAsc(String texto);

    @Query("SELECT p FROM Proveedor p WHERE p.razonSocial ILIKE '%' || :texto || '%' " +
            "OR p.ruc ILIKE '%' || :texto || '%' " +
            "ORDER BY p.razonSocial ASC")
    List<Proveedor> buscar(@Param("texto") String texto);

    long countByActivoTrue();
}
