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

/**
 * M11 Caja. Un turno de cobro de la farmacia: se abre con un monto inicial y se
 * cierra contando el efectivo (arqueo). Solo puede existir una caja abierta a
 * la vez; la base de datos lo garantiza con un indice unico parcial.
 */
@Entity
@Table(name = "caja", schema = "farmamed")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"usuarioApertura", "usuarioCierre"})
@EqualsAndHashCode(exclude = {"usuarioApertura", "usuarioCierre"})
public class Caja {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private Estado estado = Estado.ABIERTA;

    @Column(name = "monto_inicial", nullable = false, precision = 12, scale = 2)
    private BigDecimal montoInicial;

    @Column(name = "fecha_apertura", nullable = false, updatable = false)
    private LocalDateTime fechaApertura;

    // LAZY: la caja se bloquea con FOR UPDATE y un join externo sobre una FK nula lo impediria
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_apertura_id", nullable = false, foreignKey = @ForeignKey(name = "fk_caja_usuario_apertura"))
    private Usuario usuarioApertura;

    @Column(name = "observacion_apertura", length = 255)
    private String observacionApertura;

    @Column(name = "fecha_cierre")
    private LocalDateTime fechaCierre;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_cierre_id", foreignKey = @ForeignKey(name = "fk_caja_usuario_cierre"))
    private Usuario usuarioCierre;

    @Column(name = "monto_esperado", precision = 12, scale = 2)
    private BigDecimal montoEsperado;

    @Column(name = "monto_contado", precision = 12, scale = 2)
    private BigDecimal montoContado;

    @Column(precision = 12, scale = 2)
    private BigDecimal diferencia;

    @Column(name = "observacion_cierre", length = 255)
    private String observacionCierre;

    @PrePersist
    protected void onCreate() {
        if (fechaApertura == null) {
            fechaApertura = LocalDateTime.now();
        }
    }

    public enum Estado {
        ABIERTA,
        CERRADA
    }
}
