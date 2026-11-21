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
public class OrdenCompraDetalleResponse {
    private Long id;
    private Long medicamentoId;
    private String medicamentoCodigo;
    private String medicamentoNombre;
    private String unidadMedida;
    private Integer cantidad;
    private Integer stockActual;
    private Boolean sugerido;
}
