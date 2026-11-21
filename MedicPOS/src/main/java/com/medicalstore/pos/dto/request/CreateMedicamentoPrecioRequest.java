package com.medicalstore.pos.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
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
public class CreateMedicamentoPrecioRequest {

    @NotNull(message = "El medicamento es obligatorio")
    private Long medicamentoId;

    @NotNull(message = "El precio de venta es obligatorio")
    @DecimalMin(value = "0.01", message = "El precio debe ser mayor a cero")
    private BigDecimal precioVenta;

    @NotNull(message = "La fecha de inicio de vigencia es obligatoria")
    private LocalDate vigenteDesde;

    private LocalDate vigenteHasta;
}