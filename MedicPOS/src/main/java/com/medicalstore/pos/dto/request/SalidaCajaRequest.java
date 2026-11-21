package com.medicalstore.pos.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

/** Retiro de dinero de la caja. El concepto es obligatorio para poder auditarlo. */
@Data
public class SalidaCajaRequest {
    @NotNull(message = "El monto es obligatorio")
    @DecimalMin(value = "0.01", message = "El monto debe ser mayor a cero")
    @Digits(integer = 10, fraction = 2, message = "El monto admite hasta 2 decimales")
    private BigDecimal monto;

    @NotBlank(message = "El concepto del retiro es obligatorio")
    @Size(max = 255, message = "El concepto no debe superar 255 caracteres")
    private String concepto;
}
