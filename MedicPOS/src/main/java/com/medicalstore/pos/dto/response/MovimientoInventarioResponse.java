package com.medicalstore.pos.dto.response;

import com.medicalstore.pos.entity.MovimientoInventario;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MovimientoInventarioResponse {
    private Long id;
    private MovimientoInventario.TipoMovimiento tipoMovimiento;
    private Integer cantidad;
    private Integer stockAnterior;
    private Integer stockNuevo;
    private String referenciaTipo;
    private Long referenciaId;
    private LocalDateTime fechaHora;
    
    private Long medicamentoId;
    private String medicamentoNombre;
    private Long loteId;
    private String numeroLote;
    private String usuarioNombre;
}
