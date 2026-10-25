package com.medicalstore.pos.dto.request;

import com.medicalstore.pos.entity.Receta;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

/**
 * RF-05A / RF-05B: receta de paciente asegurado o particular (RN05.0).
 */
@Data
public class CreateRecetaRequest {
    @NotNull(message = "El paciente es obligatorio")
    private Long pacienteId;
    
    @NotBlank(message = "El numero de receta es obligatorio")
    @Size(max = 50, message = "El numero de receta no debe superar 50 caracteres")
    private String numeroReceta;
    
    @NotBlank(message = "El medico responsable es obligatorio")
    @Size(max = 200, message = "El medico no debe superar 200 caracteres")
    private String medico;
    
    @NotNull(message = "El tipo de receta es obligatorio")
    private Receta.TipoReceta tipoReceta;
    
    @NotNull(message = "La fecha de receta es obligatoria")
    @PastOrPresent(message = "La fecha de receta no puede ser futura")
    private LocalDate fechaReceta;
}
