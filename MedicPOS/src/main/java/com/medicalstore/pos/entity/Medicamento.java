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
@Table(name = "medicamento", schema = "farmamed", indexes = {
    @Index(name = "idx_medicamento_codigo", columnList = "codigo", unique = true),
    @Index(name = "idx_medicamento_nombre", columnList = "nombre"),
    @Index(name = "idx_medicamento_proveedor", columnList = "proveedor_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"proveedor"})
@EqualsAndHashCode(exclude = {"proveedor"})
public class Medicamento {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "proveedor_id", nullable = false, foreignKey = @ForeignKey(name = "fk_medicamento_proveedor"))
    private Proveedor proveedor;
    
    @Column(nullable = false, unique = true, length = 30)
    private String codigo;
    
    @Column(nullable = false, length = 200)
    private String nombre;
    
    @Column(columnDefinition = "TEXT")
    private String descripcion;
    
    @Column(nullable = false, length = 30)
    private String unidadMedida;
    
    @Column(nullable = false)
    private Integer stockMinimo;
    
    @Column(nullable = false)
    private Integer stockMaximo;
    
    /**
     * Costo de adquisicion del medicamento. No es un precio de venta: el sistema
     * no cobra. Sirve para valorar el inventario y medir la merma por vencimiento.
     */
    @Column(nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal costoUnitario = BigDecimal.ZERO;
    
    @Column(nullable = false)
    @Builder.Default
    private Boolean activo = true;
    
    private Long createdBy;
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    private Long updatedBy;
    
    private LocalDateTime updatedAt;
    
    private Long deletedBy;
    
    private LocalDateTime deletedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    /**
     * Cantidad sugerida de abastecimiento: completa el stock hasta el maximo configurado.
     */
    public int getCantidadSugerida(int stockVigente) {
        int sugerida = stockMaximo - stockVigente;
        return sugerida > 0 ? sugerida : 0;
    }
    
    /**
     * Indica si el stock vigente igualo o supero el minimo configurado (RN06).
     */
    public boolean isStockCritico(int stockVigente) {
        return stockVigente <= stockMinimo;
    }
}
