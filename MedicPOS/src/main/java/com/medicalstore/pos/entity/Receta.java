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

@Entity
@Table(name = "receta", schema = "farmamed", indexes = {
    @Index(name = "idx_receta_numero", columnList = "numero_receta", unique = true),
    @Index(name = "idx_receta_paciente", columnList = "paciente_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"paciente"})
@EqualsAndHashCode(exclude = {"paciente"})
public class Receta {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "paciente_id", nullable = false, foreignKey = @ForeignKey(name = "fk_receta_paciente"))
    private Paciente paciente;
    
    @Column(nullable = false, unique = true, length = 50)
    private String numeroReceta;
    
    @Column(nullable = false, length = 200)
    private String medico;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoReceta tipoReceta;
    
    @Column(nullable = false)
    private LocalDate fechaReceta;
    
    /**
     * Codigo con el que la receta figura en el HIS del hospital.
     * Queda nullable porque la integracion con el HIS es de la Fase 2.
     */
    @Column(name = "codigo_externo", length = 50)
    private String codigoExterno;
    
    private Long createdBy;
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    /**
     * Las recetas de paciente particular generan ticket interno de atencion (RN05.0).
     */
    public boolean esParticular() {
        return tipoReceta == TipoReceta.PARTICULAR;
    }
    
    public enum TipoReceta {
        ASEGURADO,
        PARTICULAR
    }
}
