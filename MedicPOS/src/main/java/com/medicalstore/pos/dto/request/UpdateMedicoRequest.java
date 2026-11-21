package com.medicalstore.pos.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * M12 Medicos: peticion para actualizar un medico.
 */
@Data
public class UpdateMedicoRequest {

    @NotNull(message = "El nombre del medico es obligatorio")
    @jakarta.validation.constraints.NotBlank(message = "El nombre del medico es obligatorio")
    @Size(max = 200, message = "El nombre no debe superar 200 caracteres")
    private String nombre;

    @Size(max = 100, message = "La especialidad no debe superar 100 caracteres")
    private String especialidad;

    @NotNull(message = "El estado es obligatorio")
    private Boolean activo;
}