package com.medicalstore.pos.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "detalle_orden_compra", schema = "farmamed", indexes = {
    @Index(name = "idx_doc_orden", columnList = "orden_compra_id"),
    @Index(name = "idx_doc_medicamento", columnList = "medicamento_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"ordenCompra", "medicamento"})
@EqualsAndHashCode(exclude = {"ordenCompra", "medicamento"})
public class DetalleOrdenCompra {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "orden_compra_id", nullable = false, foreignKey = @ForeignKey(name = "fk_doc_orden"))
    private OrdenCompra ordenCompra;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "medicamento_id", nullable = false, foreignKey = @ForeignKey(name = "fk_doc_medicamento"))
    private Medicamento medicamento;
    
    @Column(nullable = false)
    private Integer cantidad;
    
    // Flag que identifica los items originados por el pedido sugerido automatico (RF-09)
    @Column(nullable = false)
    @Builder.Default
    private Boolean sugerido = false;
}
