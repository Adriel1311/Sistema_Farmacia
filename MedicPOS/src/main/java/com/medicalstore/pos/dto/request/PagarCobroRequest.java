package com.medicalstore.pos.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.medicalstore.pos.entity.MetodoPago;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PagarCobroRequest {

    @NotNull(message = "El método de pago es obligatorio")
    private MetodoPago metodoPago;

    @NotBlank(message = "La observación es obligatoria")
    private String observacion;
}