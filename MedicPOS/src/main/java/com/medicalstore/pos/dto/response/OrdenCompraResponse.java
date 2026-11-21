package com.medicalstore.pos.dto.response;

import com.medicalstore.pos.entity.OrdenCompra;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrdenCompraResponse {
    private Long id;
    private OrdenCompra.Estado estado;
    private String observacion;
    private LocalDateTime fechaHora;
    private LocalDateTime fechaAprobacion;
    
    private Long proveedorId;
    private String proveedorRuc;
    private String proveedorRazonSocial;
    
    private Long usuarioId;
    private String usuarioNombre;
    private String usuarioRol;
    
    private Long aprobadoPorId;
    private String aprobadoPorNombre;
    
    private List<OrdenCompraDetalleResponse> detalles;
    private Integer totalItems;
    private Integer totalUnidades;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
