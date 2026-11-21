package com.medicalstore.pos.dto.request;

import com.medicalstore.pos.entity.OrdenCompra;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Decision del Jefe de Farmacia sobre una orden (RF-10A).
 */
@Data
public class ResolverOrdenCompraRequest {
    @NotNull(message = "La decision es obligatoria")
    private OrdenCompra.Estado decision;
    
    @Size(max = 1000, message = "La observacion no debe superar 1000 caracteres")
    private String observacion;
}
