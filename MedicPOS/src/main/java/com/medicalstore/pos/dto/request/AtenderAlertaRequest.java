package com.medicalstore.pos.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * RN05.3: marcar una alerta como atendida no persiste estado, se registra en AUDITORIA.
 */
@Data
public class AtenderAlertaRequest {
    @NotBlank(message = "El tipo de alerta es obligatorio")
    private String tipo;
    
    private String referenciaId;
    
    @Size(max = 500, message = "El comentario no debe superar 500 caracteres")
    private String comentario;
}
