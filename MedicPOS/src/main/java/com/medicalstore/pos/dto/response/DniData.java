package com.medicalstore.pos.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class DniData {
    @JsonProperty("numero")
    private String numero;

    @JsonProperty("codigo_verificacion")
    private String codigoVerificacion;

    @JsonProperty("nombres")
    private String nombres;

    @JsonProperty("apellido_paterno")
    private String apellidoPaterno;

    @JsonProperty("apellido_materno")
    private String apellidoMaterno;

    @JsonProperty("nombre_completo")
    private String nombreCompleto;

    @JsonProperty("departamento")
    private String departamento;

    @JsonProperty("provincia")
    private String provincia;

    @JsonProperty("distrito")
    private String distrito;

    @JsonProperty("direccion")
    private String direccion;

    @JsonProperty("direccion_completa")
    private String direccionCompleta;

    @JsonProperty("ubigeo_reniec")
    private String ubigeoReniec;

    @JsonProperty("ubigeo_sunat")
    private String ubigeoSunat;

    @JsonProperty("ubigeo")
    private List<String> ubigeo;

    @JsonProperty("fecha_nacimiento")
    private String fechaNacimiento;

    @JsonProperty("estado_civil")
    private String estadoCivil;

    @JsonProperty("foto")
    private String foto;

    @JsonProperty("sexo")
    private String sexo;
}