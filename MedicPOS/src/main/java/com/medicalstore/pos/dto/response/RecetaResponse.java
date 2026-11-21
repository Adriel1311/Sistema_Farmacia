package com.medicalstore.pos.dto.response;

import com.medicalstore.pos.entity.Receta;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecetaResponse {
    private Long id;
    private String numeroReceta;
    private String medico;
    private Receta.TipoReceta tipoReceta;
    private LocalDate fechaReceta;
    private String codigoExterno;
    
    private Long pacienteId;
    private String pacienteDni;
    private String pacienteNombres;
    private String pacienteApellidos;
    private String pacienteNombreCompleto;
    
    // RN05.0: una receta origina como maximo una dispensacion
    private Boolean tieneDispensacion;
    private Long dispensacionId;
    
    private LocalDateTime createdAt;
}
