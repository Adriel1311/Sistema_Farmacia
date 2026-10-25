package com.medicalstore.pos.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Item de una dispensacion. El lote lo asigna el sistema por FEFO (RN02).
 */
@Data
public class DispensacionItemRequest {
    @NotNull(message = "El medicamento es obligatorio")
    private Long medicamentoId;
    
    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 1, message = "La cantidad debe ser mayor a cero")
    private Integer cantidad;
    
    // Solo el Jefe de Farmacia puede forzar un lote distinto al FEFO (RF-04)
    private Long loteId;
    
    @Size(max = 500, message = "El motivo de excepcion no debe superar 500 caracteres")
    private String motivoExcepcionFefo;
}
