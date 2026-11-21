package com.medicalstore.pos.dto.request;

import jakarta.validation.constraints.DecimalMin;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateMedicamentoPrecioRequest {

    @DecimalMin(value = "0.01", message = "El precio debe ser mayor a cero")
    private BigDecimal precioVenta;

    private LocalDate vigenteDesde;

    private LocalDate vigenteHasta;
}