package com.medicalstore.pos.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "cobro", schema = "farmamed", indexes = {
    @Index(name = "idx_cobro_dispensacion", columnList = "dispensacion_id", unique = true),
    @Index(name = "idx_cobro_estado", columnList = "estado"),
    @Index(name = "idx_cobro_fecha", columnList = "created_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"dispensacion"})
@EqualsAndHashCode(exclude = {"dispensacion"})
public class Cobro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "dispensacion_id", nullable = false, unique = true, foreignKey = @ForeignKey(name = "fk_cobro_dispensacion"))
    private Dispensacion dispensacion;

    @Column(name = "subtotal", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal subtotal = BigDecimal.ZERO;

    @Column(name = "descuento_porcentaje", nullable = false, precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal descuentoPorcentaje = BigDecimal.ZERO;

    @Column(name = "descuento_monto", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal descuentoMonto = BigDecimal.ZERO;

    @Column(name = "total", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal total = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    @Builder.Default
    private EstadoCobro estado = EstadoCobro.PENDIENTE;

    @Enumerated(EnumType.STRING)
    @Column(name = "metodo_pago", length = 30)
    private MetodoPago metodoPago;

    @Column(name = "observacion", length = 500)
    private String observacion;

    @Column(name = "pagado_en")
    private LocalDateTime pagadoEn;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pagado_por", foreignKey = @ForeignKey(name = "fk_cobro_pagado_por"))
    private Usuario pagadoPor;

    private Long createdBy;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private Long updatedBy;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public void marcarPagado(Usuario usuario, MetodoPago metodo, String observacion) {
        this.estado = EstadoCobro.PAGADO;
        this.metodoPago = metodo;
        this.observacion = observacion;
        this.pagadoEn = LocalDateTime.now();
        this.pagadoPor = usuario;
        this.updatedBy = usuario != null ? usuario.getId() : null;
    }

    public void anular(Usuario usuario) {
        this.estado = EstadoCobro.ANULADO;
        this.updatedBy = usuario != null ? usuario.getId() : null;
    }
}