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
public class ProveedorResponse {
    private Long id;
    private String ruc;
    private String razonSocial;
    private String contacto;
    private String telefono;
    private String correo;
    private Boolean activo;
    private Long medicamentosAsociados;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
