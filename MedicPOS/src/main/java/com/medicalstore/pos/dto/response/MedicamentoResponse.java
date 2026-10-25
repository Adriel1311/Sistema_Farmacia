package com.medicalstore.pos.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MedicamentoResponse {
    private Long id;
    private String codigo;
    private String nombre;
    private String descripcion;
    private String unidadMedida;
    private Long proveedorId;
    private String proveedorRazonSocial;
    private Integer stockMinimo;
    private Integer stockMaximo;
    private BigDecimal costoUnitario;
    private Boolean activo;
    
    // Stock real-time: solo cuenta lo vigente (RN06)
    private Integer stockVigente;
    private Integer stockVencido;
    private Integer cantidadSugerida;
    private Boolean stockCritico;
    private Integer lotesActivos;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
