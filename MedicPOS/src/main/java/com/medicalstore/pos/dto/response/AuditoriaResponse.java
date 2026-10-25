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
public class AuditoriaResponse {
    private Long id;
    private Long usuarioId;
    private String usuarioNombre;
    private String usuarioRol;
    private String entidad;
    private String accion;
    private String descripcion;
    private String valorAnterior;
    private String valorNuevo;
    private LocalDateTime fechaHora;
}
