package com.medicalstore.pos.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * RF-12: reporte de inventario actual disponible.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReporteInventarioResponse {
    private List<ItemInventarioResponse> items;
    private Integer totalMedicamentos;
    private Long totalLotes;
    private Long totalUnidadesVigentes;
    private Long totalUnidadesVencidas;
    private Long medicamentosStockCritico;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ItemInventarioResponse {
        private Long medicamentoId;
        private String codigo;
        private String nombre;
        private String unidadMedida;
        private String proveedorRazonSocial;
        private Integer stockVigente;
        private Integer stockVencido;
        private Integer stockMinimo;
        private Integer stockMaximo;
        private Boolean stockCritico;
        private Integer cantidadSugerida;
        private Integer lotesActivos;
        private Integer lotesVencidos;
    }
}
