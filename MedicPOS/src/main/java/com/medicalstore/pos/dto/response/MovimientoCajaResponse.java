package com.medicalstore.pos.dto.response;

import com.medicalstore.pos.entity.MovimientoCaja;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MovimientoCajaResponse {
    private Long id;
    private Long cajaId;
    private MovimientoCaja.Tipo tipo;
    /** Siempre positivo; usar esIngreso para saber si suma o resta. */
    private BigDecimal monto;
    private Boolean esIngreso;
    private String concepto;
    private LocalDateTime fechaHora;
    private String usuario;

    // Solo VENTA y DEVOLUCION: datos de la boleta
    private Long dispensacionId;
    private String numeroTicket;
    private String numeroReceta;
    private String pacienteNombreCompleto;
    private String pacienteDni;
    private List<ItemVenta> items;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ItemVenta {
        private String medicamentoNombre;
        private String numeroLote;
        private Integer cantidad;
        private BigDecimal precioUnitario;
        private BigDecimal subtotal;
    }
}
