package com.medicalstore.pos.repository;

import com.medicalstore.pos.entity.PrecioVenta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PrecioVentaRepository extends JpaRepository<PrecioVenta, Long> {
}
