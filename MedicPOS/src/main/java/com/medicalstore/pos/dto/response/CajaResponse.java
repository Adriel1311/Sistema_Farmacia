package com.medicalstore.pos.dto.response;

import com.medicalstore.pos.entity.Caja;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CajaResponse {
    private Long id;
    private Caja.Estado estado;

    private BigDecimal montoInicial;
    private LocalDateTime fechaApertura;
    private String usuarioApertura;
    private String observacionApertura;

    // Totales calculados desde el libro de movimientos
    private BigDecimal totalVentas;
    private Integer cantidadVentas;
    private BigDecimal totalDevoluciones;
    private BigDecimal totalSalidas;
    /** Ventas menos devoluciones: lo realmente recaudado por boletas. */
    private BigDecimal totalRecaudado;
    /** Efectivo que debe haber en caja: inicial + recaudado - salidas. */
    private BigDecimal saldoActual;

    // Arqueo (solo caja cerrada)
    private LocalDateTime fechaCierre;
    private String usuarioCierre;
    private BigDecimal montoEsperado;
    private BigDecimal montoContado;
    private BigDecimal diferencia;
    private String observacionCierre;
}
