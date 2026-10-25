package com.medicalstore.pos.dto.response;

import com.medicalstore.pos.entity.Dispensacion;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DispensacionResponse {
    private Long id;
    private String numeroTicket;
    private Dispensacion.Estado estado;
    private LocalDateTime fechaHora;
    
    private Long recetaId;
    private String numeroReceta;
    private String medico;
    private String tipoReceta;
    private LocalDate fechaReceta;
    private String pacienteDni;
    private String pacienteNombreCompleto;
    
    private Long usuarioId;
    private String usuarioNombre;
    private String usuarioRol;
    
    private List<DispensacionDetalleResponse> detalles;
    private Integer totalItems;
    private Integer totalUnidades;
    
    private LocalDateTime createdAt;
}
