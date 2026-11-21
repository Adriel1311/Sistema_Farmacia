package com.medicalstore.pos.dto.response;

import com.medicalstore.pos.entity.Lote;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoteResponse {
    private Long id;
    private Long medicamentoId;
    private String medicamentoCodigo;
    private String medicamentoNombre;
    private String unidadMedida;
    private String proveedorRazonSocial;
    private String numeroLote;
    private LocalDate fechaIngreso;
    private LocalDate fechaVencimiento;
    private Integer cantidadInicial;
    private Integer cantidadActual;
    private Lote.Estado estado;
    
    // Semaforo de vencimiento RN04
    private Lote.EstadoVencimiento estadoVencimiento;
    private Long diasRestantes;
    
    private Long createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
