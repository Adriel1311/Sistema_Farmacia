package com.medicalstore.pos.dto.request;

import com.medicalstore.pos.entity.BajaInventario;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * RF-14: baja de inventario sin eliminar informacion historica (RN06b).
 */
@Data
public class RegistrarBajaRequest {
    @NotNull(message = "El lote es obligatorio")
    private Long loteId;
    
    @NotNull(message = "El motivo de baja es obligatorio")
    private BajaInventario.Motivo motivo;
    
    @NotNull(message = "La cantidad es obligatoria")
    @Positive(message = "La cantidad debe ser mayor a cero")
    private Integer cantidad;
    
    @Size(max = 500, message = "La observacion no debe superar 500 caracteres")
    private String observacion;
}
