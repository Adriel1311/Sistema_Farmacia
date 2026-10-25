package com.medicalstore.pos.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * RF-06: registro de dispensacion. No se permiten dispensaciones vacias (RN05.0).
 */
@Data
public class CreateDispensacionRequest {
    @NotNull(message = "La receta es obligatoria")
    private Long recetaId;
    
    @NotEmpty(message = "La dispensacion debe tener al menos un medicamento")
    @Valid
    private List<DispensacionItemRequest> items;
}
