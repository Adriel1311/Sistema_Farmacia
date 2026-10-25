package com.medicalstore.pos.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

@Entity
@Table(name = "auditoria", schema = "farmamed", indexes = {
    @Index(name = "idx_auditoria_usuario", columnList = "usuario_id"),
    @Index(name = "idx_auditoria_fecha", columnList = "fecha_hora")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"usuario"})
@EqualsAndHashCode(exclude = {"usuario"})
public class Auditoria {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false, foreignKey = @ForeignKey(name = "fk_auditoria_usuario"))
    private Usuario usuario;
    
    @Column(nullable = false, length = 100)
    private String entidad;
    
    @Column(nullable = false, length = 50)
    private String accion;
    
    @Column(columnDefinition = "TEXT")
    private String valorAnterior;
    
    @Column(columnDefinition = "TEXT")
    private String valorNuevo;
    
    @Column(name = "fecha_hora", nullable = false, updatable = false)
    private LocalDateTime fechaHora;
    
    private String descripcion;
    
    @PrePersist
    protected void onCreate() {
        fechaHora = LocalDateTime.now();
    }
    
    public enum Accion {
        // Catalogo
        MEDICAMENTO_CREADO,
        MEDICAMENTO_ACTUALIZADO,
        MEDICAMENTO_DESACTIVADO,
        // Inventario
        LOTE_INGRESADO,
        LOTE_ACTUALIZADO,
        BAJA_REGISTRADA,
        // Dispensacion
        RECETA_REGISTRADA,
        DISPENSACION_REGISTRADA,
        DISPENSACION_ANULADA,
        EXCEPCION_FEFO,
        // Abastecimiento
        PROVEEDOR_CREADO,
        PROVEEDOR_ACTUALIZADO,
        PROVEEDOR_DESACTIVADO,
        ORDEN_CREADA,
        ORDEN_APROBADA,
        ORDEN_RECHAZADA,
        // Seguridad
        USUARIO_CREADO,
        USUARIO_ACTUALIZADO,
        USUARIO_DESACTIVADO,
        USUARIO_ACTIVADO,
        CONTRASENA_ACTUALIZADA,
        // Configuracion y avisos
        CONFIGURACION_ACTUALIZADA,
        ALERTA_ATENDIDA
    }
}
