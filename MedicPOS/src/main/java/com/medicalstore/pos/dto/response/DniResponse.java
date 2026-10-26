package com.medicalstore.pos.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class DniResponse {
    @JsonProperty("status")
    private Integer status;

    @JsonProperty("message")
    private String message;

    @JsonProperty("success")
    private Boolean success;

    @JsonProperty("data")
    private DniData data;

    @JsonProperty("fuente")
    private Integer fuente;

    // Campos planos para compatibilidad con código existente
    @JsonProperty("nombres")
    private String nombres;

    @JsonProperty("apellidos")
    private String apellidos;

    @JsonProperty("dni")
    private String dni;

    @JsonProperty("nombreCompleto")
    private String nombreCompleto;

    @JsonProperty("direccion")
    private String direccion;

    // Métodos helper para obtener datos normalizados
    public String getNombresNormalizados() {
        if (data != null && data.getNombres() != null) {
            return data.getNombres();
        }
        return nombres;
    }

    public String getApellidosNormalizados() {
        if (data != null) {
            StringBuilder sb = new StringBuilder();
            if (data.getApellidoPaterno() != null) sb.append(data.getApellidoPaterno());
            if (data.getApellidoMaterno() != null) {
                if (sb.length() > 0) sb.append(" ");
                sb.append(data.getApellidoMaterno());
            }
            return sb.toString();
        }
        return apellidos;
    }

    public String getDniNormalizado() {
        if (data != null && data.getNumero() != null) {
            return data.getNumero();
        }
        return dni;
    }

    public String getNombreCompletoNormalizado() {
        if (data != null && data.getNombreCompleto() != null) {
            return data.getNombreCompleto();
        }
        return nombreCompleto;
    }

    public String getDireccionNormalizada() {
        if (data != null && data.getDireccionCompleta() != null) {
            return data.getDireccionCompleta();
        }
        if (data != null && data.getDireccion() != null) {
            return data.getDireccion();
        }
        return direccion;
    }
}