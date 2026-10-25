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
@Table(name = "baja_inventario", schema = "farmamed", indexes = {
    @Index(name = "idx_baja_lote", columnList = "lote_id"),
    @Index(name = "idx_baja_usuario", columnList = "usuario_id"),
    @Index(name = "idx_baja_fecha", columnList = "fecha_hora")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"lote", "usuario"})
@EqualsAndHashCode(exclude = {"lote", "usuario"})
public class BajaInventario {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "lote_id", nullable = false, foreignKey = @ForeignKey(name = "fk_baja_lote"))
    private Lote lote;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "usuario_id", nullable = false, foreignKey = @ForeignKey(name = "fk_baja_usuario"))
    private Usuario usuario;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Motivo motivo;
    
    @Column(nullable = false)
    private Integer cantidad;
    
    @Column(length = 500)
    private String observacion;
    
    /**
     * Valor de la perdida en la baja, calculado sobre el costo de adquisicion.
     * Mide el costo de la merma; no es un cobro al paciente.
     */
    @Column(nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal valorPerdida = BigDecimal.ZERO;
    
    @Column(name = "fecha_hora", nullable = false, updatable = false)
    private LocalDateTime fechaHora;
    
    @PrePersist
    protected void onCreate() {
        fechaHora = LocalDateTime.now();
    }
    
    public enum Motivo {
        VENCIDO_EN_ESTANTE,
        MERMA,
        DEVUELTO
    }
}
