package com.medicalstore.pos.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.Generated;
import org.hibernate.generator.EventType;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "dispensacion", schema = "farmamed", indexes = {
    @Index(name = "uq_dispensacion_receta", columnList = "receta_id", unique = true),
    @Index(name = "idx_dispensacion_usuario", columnList = "usuario_id"),
    @Index(name = "idx_dispensacion_fecha", columnList = "fecha_hora")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"receta", "usuario", "detalles"})
@EqualsAndHashCode(exclude = {"receta", "usuario", "detalles"})
public class Dispensacion {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // RN05.0: una receta origina como maximo una dispensacion (receta_id UNIQUE)
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "receta_id", nullable = false, unique = true, foreignKey = @ForeignKey(name = "fk_dispensacion_receta"))
    private Receta receta;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "usuario_id", nullable = false, foreignKey = @ForeignKey(name = "fk_dispensacion_usuario"))
    private Usuario usuario;
    
    // RN05.0: no se permiten dispensaciones vacias
    @OneToMany(mappedBy = "dispensacion", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @Builder.Default
    private List<DetalleDispensacion> detalles = new ArrayList<>();
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private Estado estado = Estado.COMPLETADA;
    
    @Column(name = "fecha_hora", nullable = false, updatable = false)
    private LocalDateTime fechaHora;
    
    /**
     * Ticket interno de atencion (RN05.0). Lo genera la secuencia de la base de
     * datos, asi el numero nunca se repite. Lo reciben las dos clases de receta,
     * ASEGURADO y PARTICULAR.
     * No es una boleta SUNAT ni un comprobante de cobro: el sistema no cobra.
     */
    @Column(name = "numero_ticket", nullable = false, insertable = false, updatable = false)
    @Generated(event = EventType.INSERT)
    private String numeroTicket;
    
    private Long createdBy;
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        fechaHora = LocalDateTime.now();
    }
    
    public void agregarDetalle(DetalleDispensacion detalle) {
        detalles.add(detalle);
        detalle.setDispensacion(this);
    }
    
    public enum Estado {
        COMPLETADA,
        ANULADA
    }
}
