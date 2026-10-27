package com.medicalstore.pos.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * M12 Medicos: respuesta de medico.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MedicoResponse {

    private Long id;
    private String nombre;
    private String especialidad;
    private Boolean activo;
    private java.time.LocalDateTime createdAt;
    private java.time.LocalDateTime updatedAt;
}