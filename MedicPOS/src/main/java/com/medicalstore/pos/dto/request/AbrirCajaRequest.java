package com.medicalstore.pos.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class AbrirCajaRequest {
    @NotNull(message = "El monto inicial es obligatorio")
    @DecimalMin(value = "0.00", message = "El monto inicial no puede ser negativo")
    @Digits(integer = 10, fraction = 2, message = "El monto admite hasta 2 decimales")
    private BigDecimal montoInicial;

    @Size(max = 255, message = "La observacion no debe superar 255 caracteres")
    private String observacion;
}
