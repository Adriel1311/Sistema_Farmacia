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
import java.util.ArrayList;
import java.util.List;

/**
 * Libro de la caja. El monto siempre es positivo; el signo lo da el tipo:
 * APERTURA y VENTA suman, SALIDA y DEVOLUCION restan.
 */
@Entity
@Table(name = "movimiento_caja", schema = "farmamed")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"caja", "dispensacion", "usuario", "items"})
@EqualsAndHashCode(exclude = {"caja", "dispensacion", "usuario", "items"})
public class MovimientoCaja {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "caja_id", nullable = false, foreignKey = @ForeignKey(name = "fk_mc_caja"))
    private Caja caja;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Tipo tipo;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal monto;

    @Column(nullable = false, length = 255)
    private String concepto;

    // Solo en VENTA y DEVOLUCION: la boleta (dispensacion) que origino el movimiento
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dispensacion_id", foreignKey = @ForeignKey(name = "fk_mc_dispensacion"))
    private Dispensacion dispensacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false, foreignKey = @ForeignKey(name = "fk_mc_usuario"))
    private Usuario usuario;

    @Column(name = "fecha_hora", nullable = false, updatable = false)
    private LocalDateTime fechaHora;

    /** Detalle de la venta (solo en VENTA): medicamentos, cantidades y precios cobrados. */
    @OneToMany(mappedBy = "movimiento", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<MovimientoCajaItem> items = new ArrayList<>();

    public void agregarItem(MovimientoCajaItem item) {
        items.add(item);
        item.setMovimiento(this);
    }

    @PrePersist
    protected void onCreate() {
        if (fechaHora == null) {
            fechaHora = LocalDateTime.now();
        }
    }

    /** true si el movimiento aumenta el efectivo de la caja. */
    public boolean esIngreso() {
        return tipo == Tipo.APERTURA || tipo == Tipo.VENTA;
    }

    public enum Tipo {
        APERTURA,
        VENTA,
        SALIDA,
        DEVOLUCION
    }
}
