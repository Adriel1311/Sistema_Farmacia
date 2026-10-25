package com.medicalstore.pos.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConfiguracionResponse {
    private Long id;
    private String clave;
    private String valor;
    private String descripcion;
    private Boolean activo;
    private String categoria;
    private String tipoDato;
    private LocalDateTime updatedAt;
    private String updatedByNombre;
}
