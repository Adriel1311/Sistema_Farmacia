package com.medicalstore.pos.dto.response;

import com.medicalstore.pos.entity.Cobro;
import com.medicalstore.pos.entity.Dispensacion;
import com.medicalstore.pos.entity.EstadoCobro;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DispensacionResponse {
    private Long id;
    private String numeroTicket;
    private Dispensacion.Estado estado;
    private LocalDateTime fechaHora;
    
    private Long recetaId;
    private String numeroReceta;
    private String medico;
    private String tipoReceta;
    private LocalDate fechaReceta;
    private String pacienteDni;
    private String pacienteNombreCompleto;
    
    private Long usuarioId;
    private String usuarioNombre;
    private String usuarioRol;
    
    private List<DispensacionDetalleResponse> detalles;
    private Integer totalItems;
    private Integer totalUnidades;
    
    private LocalDateTime createdAt;
    
    // Cobro
    private CobroResponse cobro;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CobroResponse {
        private Long id;
        private BigDecimal subtotal;
        private BigDecimal descuentoPorcentaje;
        private BigDecimal descuentoMonto;
        private BigDecimal total;
        private EstadoCobro estado;
    }
}