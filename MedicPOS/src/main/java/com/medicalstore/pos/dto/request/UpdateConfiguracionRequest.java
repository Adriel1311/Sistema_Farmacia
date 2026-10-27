package com.medicalstore.pos.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Map;

/**
 * M11 Configuracion: parametros globales del sistema.
 * Solo el Jefe de Farmacia puede modificarlos y todo cambio queda auditado (RN04, RN06).
 */
@Data
public class UpdateConfiguracionRequest {
    @NotNull(message = "Debe enviar al menos un parametro")
    @NotEmpty(message = "Debe enviar al menos un parametro")
    private Map<String, String> valores;
}
