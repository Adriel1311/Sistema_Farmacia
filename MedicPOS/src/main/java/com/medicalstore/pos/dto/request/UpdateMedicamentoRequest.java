package com.medicalstore.pos.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class UpdateMedicamentoRequest {
    @NotBlank(message = "El codigo es obligatorio")
    @Size(max = 30, message = "El codigo no debe superar 30 caracteres")
    private String codigo;
    
    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 200, message = "El nombre no debe superar 200 caracteres")
    private String nombre;
    
    private String descripcion;
    
    @NotBlank(message = "La unidad de medida es obligatoria")
    private String unidadMedida;
    
    @NotNull(message = "El proveedor habitual es obligatorio")
    private Long proveedorId;
    
    @NotNull(message = "El stock minimo es obligatorio")
    @Min(value = 1, message = "El stock minimo debe ser mayor a cero")
    private Integer stockMinimo;
    
    @NotNull(message = "El stock maximo es obligatorio")
    @Min(value = 1, message = "El stock maximo debe ser mayor a cero")
    private Integer stockMaximo;
    
    /**
     * Costo de adquisicion. Sirve para valorar el inventario y medir la merma.
     * El sistema no cobra, asi que este valor no es un precio de venta.
     */
    @DecimalMin(value = "0.00", message = "El costo unitario no puede ser negativo")
    private BigDecimal costoUnitario;
    
    @NotNull(message = "El estado del medicamento es obligatorio")
    private Boolean activo;
}
