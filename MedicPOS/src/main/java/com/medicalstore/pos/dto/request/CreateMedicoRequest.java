package com.medicalstore.pos.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

/**
 * M12 Medicos: peticion para crear un medico.
 */
@Data
public class CreateMedicoRequest {

    @NotBlank(message = "El nombre del medico es obligatorio")
    @Size(max = 200, message = "El nombre no debe superar 200 caracteres")
    private String nombre;

    @Size(max = 100, message = "La especialidad no debe superar 100 caracteres")
    private String especialidad;
}