package com.medicalstore.pos.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DispensacionDetalleResponse {
    private Long id;
    private Long medicamentoId;
    private String medicamentoCodigo;
    private String medicamentoNombre;
    private String unidadMedida;
    private Long loteId;
    private String numeroLote;
    private LocalDate fechaVencimiento;
    private String estadoVencimiento;
    private Integer cantidad;
    
    // RF-04: presente cuando el Jefe autorizo un lote distinto al FEFO
    private Boolean excepcionFefo;
    private String motivoExcepcionFefo;
}
