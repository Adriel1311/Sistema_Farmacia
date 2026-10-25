package com.medicalstore.pos.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * RF-12: reporte de consumo por medicamento en un rango de fechas.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReporteConsumoResponse {
    private LocalDate desde;
    private LocalDate hasta;
    private List<ItemConsumoResponse> items;
    private Integer totalMedicamentos;
    private Integer totalUnidades;
    private Long totalDispensaciones;
    private Integer diasReporte;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ItemConsumoResponse {
        private Long medicamentoId;
        private String codigo;
        private String nombre;
        private String unidadMedida;
        private Integer unidadesConsumidas;
        private Integer dispensaciones;
        private Integer promedioPorDispensacion;
    }
}
