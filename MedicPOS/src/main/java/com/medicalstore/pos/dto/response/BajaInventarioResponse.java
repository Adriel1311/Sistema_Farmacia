package com.medicalstore.pos.dto.response;

import com.medicalstore.pos.entity.BajaInventario;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BajaInventarioResponse {
    private Long id;
    private BajaInventario.Motivo motivo;
    private Integer cantidad;
    private String observacion;
    private BigDecimal valorPerdida;
    private LocalDateTime fechaHora;
    
    private Long loteId;
    private String numeroLote;
    private LocalDate fechaVencimientoLote;
    
    private Long medicamentoId;
    private String medicamentoCodigo;
    private String medicamentoNombre;
    
    private Long usuarioId;
    private String usuarioNombre;
    private String usuarioRol;
}
