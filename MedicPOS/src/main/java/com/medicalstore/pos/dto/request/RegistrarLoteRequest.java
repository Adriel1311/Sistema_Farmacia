package com.medicalstore.pos.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

/**
 * RF-05: registro de ingreso de medicamentos mediante lotes (RN01, RN01b, RN01c).
 */
@Data
public class RegistrarLoteRequest {
    @NotNull(message = "El medicamento es obligatorio")
    private Long medicamentoId;
    
    @NotBlank(message = "El numero de lote es obligatorio")
    @Size(max = 50, message = "El numero de lote no debe superar 50 caracteres")
    private String numeroLote;
    
    @NotNull(message = "La fecha de ingreso es obligatoria")
    @PastOrPresent(message = "La fecha de ingreso no puede ser futura")
    private LocalDate fechaIngreso;
    
    @NotNull(message = "La fecha de vencimiento es obligatoria")
    @Future(message = "La fecha de vencimiento debe ser posterior a la fecha de ingreso")
    private LocalDate fechaVencimiento;
    
    @NotNull(message = "La cantidad es obligatoria")
    private Integer cantidad;
    
    private Long ordenCompraId;
}
