package com.medicalstore.pos.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Entity
@Table(name = "lote", schema = "farmamed", indexes = {
    @Index(name = "uq_lote_medicamento", columnList = "medicamento_id,numero_lote", unique = true),
    @Index(name = "idx_lote_vencimiento", columnList = "fecha_vencimiento"),
    @Index(name = "idx_lote_medicamento_fecha", columnList = "medicamento_id,fecha_vencimiento")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"medicamento"})
@EqualsAndHashCode(exclude = {"medicamento"})
public class Lote {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "medicamento_id", nullable = false, foreignKey = @ForeignKey(name = "fk_lote_medicamento"))
    private Medicamento medicamento;
    
    @Column(nullable = false, length = 50)
    private String numeroLote;
    
    @Column(nullable = false)
    private LocalDate fechaIngreso;
    
    @Column(nullable = false)
    private LocalDate fechaVencimiento;
    
    @Column(nullable = false)
    private Integer cantidadInicial;
    
    @Column(nullable = false)
    private Integer cantidadActual;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private Estado estado = Estado.ACTIVO;
    
    private Long createdBy;
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    private Long updatedBy;
    
    private LocalDateTime updatedAt;
    
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
     * Dias restantes hasta el vencimiento. Negativo si ya vencio.
     */
    public long getDiasRestantes() {
        return ChronoUnit.DAYS.between(LocalDate.now(), fechaVencimiento);
    }
    
    /**
     * Semaforo de vencimiento RN04. Los umbrales llegan desde
     * configuracion_sistema (vencimiento.dias.critico y
     * vencimiento.dias.proximo) para que el criterio sea el mismo en todos los
     * modulos y se pueda ajustar sin recompilar.
     */
    @Transient
    public EstadoVencimiento getEstadoVencimiento(int diasCritico, int diasProximo) {
        if (estado == Estado.BAJA) {
            return EstadoVencimiento.NEGRO;
        }
        long dias = getDiasRestantes();
        if (dias < 0) {
            return EstadoVencimiento.NEGRO;
        }
        if (dias <= diasCritico) {
            return EstadoVencimiento.ROJO;
        }
        if (dias <= diasProximo) {
            return EstadoVencimiento.AMBAR;
        }
        return EstadoVencimiento.VERDE;
    }
    
    /**
     * Un lote es vigente si no vencio, no fue dado de baja y aun tiene existencias (RN03).
     */
    public boolean isVigente() {
        return estado == Estado.ACTIVO
                && cantidadActual > 0
                && !fechaVencimiento.isBefore(LocalDate.now());
    }
    
    /**
     * Un lote vencido no puede dispensarse aunque aun tenga existencias fisicas (RN03).
     */
    public boolean isVencido() {
        return fechaVencimiento.isBefore(LocalDate.now());
    }
    
    public boolean tieneStock(int cantidad) {
        return cantidadActual >= cantidad;
    }
    
    public enum Estado {
        ACTIVO,
        VENCIDO,
        BAJA
    }
    
    public enum EstadoVencimiento {
        VERDE,    // Vigente, mas de 60 dias
        AMBAR,    // Proximo a vencer, 31 a 60 dias
        ROJO,     // Critico, 0 a 30 dias
        NEGRO     // Vencido o dado de baja
    }
}
