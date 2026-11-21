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
public class RolResponse {
    private Long id;
    private String nombre;
    private String descripcion;
    private Boolean activo;
    private Long usuariosAsignados;
    private LocalDateTime createdAt;
}
