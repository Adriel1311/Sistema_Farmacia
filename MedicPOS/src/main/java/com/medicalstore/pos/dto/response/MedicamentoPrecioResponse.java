package com.medicalstore.pos.dto.response;

import com.medicalstore.pos.entity.EstadoCobro;
import com.medicalstore.pos.entity.MetodoPago;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MedicamentoPrecioResponse {

    private Long id;
    private Long medicamentoId;
    private String medicamentoCodigo;
    private String medicamentoNombre;
    private BigDecimal precioVenta;
    private LocalDate vigenteDesde;
    private LocalDate vigenteHasta;
    private LocalDateTime createdAt;
}