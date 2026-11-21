package com.medicalstore.pos.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateProveedorRequest {
    @NotBlank(message = "El RUC es obligatorio")
    @Pattern(regexp = "^[0-9]{11}$", message = "El RUC debe tener 11 digitos")
    private String ruc;
    
    @NotBlank(message = "La razon social es obligatoria")
    @Size(max = 200, message = "La razon social no debe superar 200 caracteres")
    private String razonSocial;
    
    private String contacto;
    
    @Size(max = 30, message = "El telefono no debe superar 30 caracteres")
    private String telefono;
    
    @Email(message = "El correo no tiene un formato valido")
    @Size(max = 150, message = "El correo no debe superar 150 caracteres")
    private String correo;
}
