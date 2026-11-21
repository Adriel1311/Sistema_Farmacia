package com.medicalstore.pos.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * RF-10A: orden de compra. Logistica la crea, el Jefe de Farmacia la aprueba o rechaza (RN07).
 */
@Data
public class CreateOrdenCompraRequest {
    @NotNull(message = "El proveedor es obligatorio")
    private Long proveedorId;
    
    @NotEmpty(message = "La orden debe tener al menos un medicamento")
    @Valid
    private List<OrdenCompraItemRequest> items;
    
    private String observacion;
}
