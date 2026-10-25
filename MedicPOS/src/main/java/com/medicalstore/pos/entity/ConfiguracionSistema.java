package com.medicalstore.pos.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Parametros globales del sistema (M11 Configuracion).
 * Solo el Jefe de Farmacia puede modificarlos y todo cambio queda auditado (RN04, RN06).
 */
@Entity
@Table(name = "configuracion_sistema", schema = "farmamed")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConfiguracionSistema {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true, length = 100)
    private String clave;
    
    @Column(nullable = false, length = 255)
    private String valor;
    
    @Column(length = 255)
    private String descripcion;
    
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
}
