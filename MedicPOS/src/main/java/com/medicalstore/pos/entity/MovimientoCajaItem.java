package com.medicalstore.pos.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;

/**
 * Linea de una venta en caja: medicamento, lote, cantidad y precio congelado al cobrar.
 * Guarda el nombre y el lote como texto para que la boleta no cambie si el catalogo cambia.
 */
@Entity
@Table(name = "movimiento_caja_item", schema = "farmamed")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"movimiento"})
@EqualsAndHashCode(exclude = {"movimiento"})
public class MovimientoCajaItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "movimiento_id", nullable = false)
    private MovimientoCaja movimiento;

    @Column(name = "detalle_dispensacion_id")
    private Long detalleDispensacionId;

    @Column(name = "medicamento_id", nullable = false)
    private Long medicamentoId;

    @Column(name = "medicamento_nombre", nullable = false, length = 200)
    private String medicamentoNombre;

    @Column(name = "numero_lote", nullable = false, length = 50)
    private String numeroLote;

    @Column(nullable = false)
    private Integer cantidad;

    @Column(name = "precio_unitario", nullable = false, precision = 12, scale = 2)
    private BigDecimal precioUnitario;

    public BigDecimal getSubtotal() {
        return precioUnitario.multiply(BigDecimal.valueOf(cantidad));
    }
}
