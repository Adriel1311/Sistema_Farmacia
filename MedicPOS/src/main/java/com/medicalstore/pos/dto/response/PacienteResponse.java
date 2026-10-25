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
public class PacienteResponse {
    private Long id;
    private String dni;
    private String nombres;
    private String apellidos;
    private String codigoExterno;
    private String fullName;
    private Boolean activo;
    private LocalDateTime createdAt;
}
