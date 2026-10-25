package com.medicalstore.pos.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "paciente", schema = "farmamed", indexes = {
    @Index(name = "idx_paciente_dni", columnList = "dni", unique = true)
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Paciente {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true, length = 8)
    private String dni;
    
    @Column(nullable = false, length = 100)
    private String nombres;
    
    @Column(nullable = false, length = 100)
    private String apellidos;
    
    /**
     * Codigo con el que el paciente figura en el HIS del hospital.
     * Queda nullable porque la integracion con el HIS es de la Fase 2.
     */
    @Column(name = "codigo_externo", length = 50)
    private String codigoExterno;
    
    @Column(nullable = false)
    @Builder.Default
    private Boolean activo = true;
    
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
    
    public String getNombreCompleto() {
        return nombres + " " + apellidos;
    }
}
