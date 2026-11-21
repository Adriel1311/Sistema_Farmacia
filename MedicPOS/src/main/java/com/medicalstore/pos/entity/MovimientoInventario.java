package com.medicalstore.pos.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * Kardex centralizado de movimientos de inventario (RN09).
 * Toda operacion que modifica existencias queda registrada con su trazabilidad.
 */
@Entity
@Table(name = "movimiento_inventario", schema = "farmamed", indexes = {
    @Index(name = "idx_mov_medicamento", columnList = "medicamento_id"),
    @Index(name = "idx_mov_lote", columnList = "lote_id"),
    @Index(name = "idx_mov_usuario", columnList = "usuario_id"),
    @Index(name = "idx_mov_fecha", columnList = "fecha_hora")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"medicamento", "lote", "usuario"})
@EqualsAndHashCode(exclude = {"medicamento", "lote", "usuario"})
public class MovimientoInventario {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "medicamento_id", nullable = false, foreignKey = @ForeignKey(name = "fk_mov_medicamento"))
    private Medicamento medicamento;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "lote_id", nullable = false, foreignKey = @ForeignKey(name = "fk_mov_lote"))
    private Lote lote;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "usuario_id", nullable = false, foreignKey = @ForeignKey(name = "fk_mov_usuario"))
    private Usuario usuario;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoMovimiento tipoMovimiento;
    
    @Column(nullable = false)
    private Integer cantidad;
    
    @Column(nullable = false)
    private Integer stockAnterior;
    
    @Column(nullable = false)
    private Integer stockNuevo;
    
    @Column(nullable = false, length = 50)
    private String referenciaTipo;
    
    @Column(nullable = false)
    private Long referenciaId;
    
    @Column(name = "fecha_hora", nullable = false, updatable = false)
    private LocalDateTime fechaHora;
    
    @PrePersist
    protected void onCreate() {
        fechaHora = LocalDateTime.now();
    }
    
    public enum TipoMovimiento {
        ENTRADA,
        SALIDA,
        BAJA,
        AJUSTE
    }
}
