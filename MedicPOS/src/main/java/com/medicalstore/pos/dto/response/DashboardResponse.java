package com.medicalstore.pos.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * RF-13: indicadores consolidados del panel operativo.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardResponse {
    
    // Indicadores principales
    private Long lotesCriticos;
    private Long lotesProximosAVencer;
    private Long lotesVencidos;
    private Long medicamentosStockCritico;
    private Long ordenesSugeridas;
    private Long dispensacionesHoy;
    private Long medicamentosActivos;
    private Long proveedoresActivos;
    private Long alertasTotales;
    private Long alertasAltaPrioridad;
    
    // Semaforo operativo (RN04) leido de la configuracion vigente
    private Integer diasCritico;
    private Integer diasProximo;
    
    // Detalle de vencimientos proximos
    private List<LoteResponse> proximosAVencer;
    
    // Detalle de stock critico con pedido sugerido (RF-09)
    private List<SugerenciaCompraResponse> suggestions;
    
    private String generadoEn;
}
