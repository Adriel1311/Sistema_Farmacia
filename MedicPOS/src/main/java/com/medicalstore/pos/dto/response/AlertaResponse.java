package com.medicalstore.pos.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * RN05.1: los 5 avisos que existen. No se persisten, se calculan al abrir cada pantalla.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlertaResponse {
    private String clave;
    private TipoAlerta tipo;
    private Prioridad prioridad;
    private String titulo;
    private String mensaje;
    
    private Long referenciaId;
    private String referenciaTipo;
    
    // Contexto variable segun el tipo: diasRestantes, stockMinimo, cantidadSugerida...
    private Map<String, Object> datos;
    
    // Estado logico de visualizacion (RN05.3): "atendida" se deduce de la auditoria
    private Boolean atendida;
    private LocalDateTime atendidaEn;
    private String atendidaPor;
    
    public enum TipoAlerta {
        VENCIMIENTO,
        STOCK,
        ORDEN
    }
    
    public enum Prioridad {
        ALTA,
        MEDIA
    }
}
