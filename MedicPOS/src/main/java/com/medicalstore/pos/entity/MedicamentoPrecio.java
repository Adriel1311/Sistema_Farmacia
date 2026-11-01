package com.medicalstore.pos.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "medicamento_precio", schema = "farmamed", indexes = {
    @Index(name = "idx_mp_medicamento_vigencia", columnList = "medicamento_id, vigente_desde, vigente_hasta"),
    @Index(name = "idx_mp_vigente_hoy", columnList = "medicamento_id, vigente_desde")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"medicamento"})
@EqualsAndHashCode(exclude = {"medicamento"})
public class MedicamentoPrecio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "medicamento_id", nullable = false, foreignKey = @ForeignKey(name = "fk_mp_medicamento"))
    private Medicamento medicamento;

    @Column(name = "precio_venta", nullable = false, precision = 12, scale = 2)
    private BigDecimal precioVenta;

    @Column(name = "vigente_desde", nullable = false)
    private LocalDate vigenteDesde;

    @Column(name = "vigente_hasta")
    private LocalDate vigenteHasta;

    private Long createdBy;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private Long updatedBy;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (vigenteDesde == null) {
            vigenteDesde = LocalDate.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public boolean estaVigente(LocalDate fecha) {
        if (vigenteDesde.isAfter(fecha)) {
            return false;
        }
        return vigenteHasta == null || !vigenteHasta.isBefore(fecha);
    }
}