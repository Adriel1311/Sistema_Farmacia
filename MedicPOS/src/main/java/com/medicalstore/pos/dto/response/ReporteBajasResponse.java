package com.medicalstore.pos.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * RF-12: reporte de bajas por caducidad, merma o devolucion en un rango de fechas.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReporteBajasResponse {
    private LocalDate desde;
    private LocalDate hasta;
    private List<BajaInventarioResponse> bajas;
    private Integer totalRegistros;
    private Integer totalUnidades;
    private Integer totalVencidoEnEstante;
    private Integer totalMerma;
    private Integer totalDevuelto;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ResumenPorMedicamento {
        private Long medicamentoId;
        private String codigo;
        private String nombre;
        private Integer unidadesDadasDeBaja;
    }
    
    private List<ResumenPorMedicamento> resumenPorMedicamento;
}
