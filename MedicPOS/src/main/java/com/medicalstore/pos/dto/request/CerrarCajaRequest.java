package com.medicalstore.pos.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

/** Cierre con arqueo: el efectivo realmente contado en la caja. */
@Data
public class CerrarCajaRequest {
    @NotNull(message = "El monto contado es obligatorio")
    @DecimalMin(value = "0.00", message = "El monto contado no puede ser negativo")
    @Digits(integer = 10, fraction = 2, message = "El monto admite hasta 2 decimales")
    private BigDecimal montoContado;

    @Size(max = 255, message = "La observacion no debe superar 255 caracteres")
    private String observacion;
}
