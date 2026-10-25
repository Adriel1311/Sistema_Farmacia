package com.medicalstore.pos.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "detalle_dispensacion", schema = "farmamed", indexes = {
    @Index(name = "idx_dd_dispensacion", columnList = "dispensacion_id"),
    @Index(name = "idx_dd_medicamento", columnList = "medicamento_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"dispensacion", "medicamento", "lote"})
@EqualsAndHashCode(exclude = {"dispensacion", "medicamento", "lote"})
public class DetalleDispensacion {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dispensacion_id", nullable = false, foreignKey = @ForeignKey(name = "fk_dd_dispensacion"))
    private Dispensacion dispensacion;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "medicamento_id", nullable = false, foreignKey = @ForeignKey(name = "fk_dd_medicamento"))
    private Medicamento medicamento;
    
    // Lote asignado por el sistema segun FEFO, o por excepcion autorizada del Jefe
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "lote_id", nullable = false, foreignKey = @ForeignKey(name = "fk_dd_lote"))
    private Lote lote;
    
    @Column(nullable = false)
    private Integer cantidad;
    
    // RN04: motivo registrado cuando el Jefe autoriza un lote distinto al FEFO
    @Column(length = 500)
    private String motivoExcepcionFefo;
}
