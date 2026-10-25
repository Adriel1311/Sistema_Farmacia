package com.medicalstore.pos.dto.request;

import com.medicalstore.pos.dto.response.SugerenciaCompraResponse;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * RF-09: convierte el pedido sugerido en una orden formal.
 * Solo se aceptan sugerencias de un mismo proveedor (RN07).
 */
@Data
public class CrearDesdeSugerenciaRequest {
    
    @NotEmpty(message = "Debe enviar al menos una sugerencia")
    private List<SugerenciaCompraResponse> sugerencias;
    
    @Size(max = 1000, message = "La observacion no debe superar 1000 caracteres")
    private String observacion;
}
