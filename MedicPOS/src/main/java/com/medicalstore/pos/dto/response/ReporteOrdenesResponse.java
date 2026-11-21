package com.medicalstore.pos.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * RF-12: historial de ordenes de compra.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReporteOrdenesResponse {
    private LocalDate desde;
    private LocalDate hasta;
    private List<OrdenCompraResponse> ordenes;
    private Integer totalOrdenes;
    private Integer totalUnidades;
    private Long totalBorrador;
    private Long totalSugerida;
    private Long totalAprobada;
    private Long totalRechazada;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ResumenPorProveedor {
        private Long proveedorId;
        private String razonSocial;
        private String ruc;
        private Integer ordenes;
        private Integer unidades;
    }
    
    private List<ResumenPorProveedor> resumenPorProveedor;
}
