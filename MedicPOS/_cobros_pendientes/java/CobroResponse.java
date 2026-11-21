package com.medicalstore.pos.dto.response;

import com.medicalstore.pos.entity.EstadoCobro;
import com.medicalstore.pos.entity.MetodoPago;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CobroResponse {

    private Long id;
    private Long dispensacionId;
    private String numeroTicket;
    private String tipoReceta;
    private String pacienteNombre;
    private BigDecimal subtotal;
    private BigDecimal descuentoPorcentaje;
    private BigDecimal descuentoMonto;
    private BigDecimal total;
    private EstadoCobro estado;
    private MetodoPago metodoPago;
    private String observacion;
    private LocalDateTime pagadoEn;
    private String pagadoPorNombre;
    private LocalDateTime createdAt;
}