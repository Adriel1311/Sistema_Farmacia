package com.medicalstore.pos.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * RF-09: pedido sugerido. Nace del stock critico y ya propone proveedor y cantidad (RN07).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SugerenciaCompraResponse {
    private Long medicamentoId;
    private String codigo;
    private String nombre;
    private String unidadMedida;
    private Integer stockActual;
    private Integer stockMinimo;
    private Integer stockMaximo;
    private Integer cantidadSugerida;
    private Long proveedorId;
    private String proveedorRazonSocial;
}
