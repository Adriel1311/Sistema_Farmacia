package com.medicalstore.pos.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Precio de venta al publico de un medicamento (soles). Vive en su propia tabla para no
 * modificar la tabla medicamento. Solo se cobra en recetas PARTICULAR.
 */
@Entity
@Table(name = "precio_venta", schema = "farmamed")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PrecioVenta {

    @Id
    @Column(name = "medicamento_id")
    private Long medicamentoId;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal precio;

    @Column(name = "updated_by")
    private Long updatedBy;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
