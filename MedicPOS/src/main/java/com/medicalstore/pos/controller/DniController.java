package com.medicalstore.pos.controller;

import com.medicalstore.pos.dto.response.DniResponse;
import com.medicalstore.pos.service.DniService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Pattern;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/dni")
@Tag(name = "DNI", description = "Consulta DNI vía Factiliza (RENIEC)")
public class DniController {

    private final DniService dniService;

    public DniController(DniService dniService) {
        this.dniService = dniService;
    }

    @GetMapping("/{dni}")
    @Operation(summary = "Consultar DNI en RENIEC vía Factiliza",
            description = "Retorna nombres, apellidos y DNI. Valida que no exista en BD local. Cache 24h.")
    public ResponseEntity<DniResponse> consultarDni(
            @Parameter(description = "DNI de 8 dígitos", example = "12345678")
            @PathVariable @Pattern(regexp = "^\\d{8}$", message = "DNI debe tener 8 dígitos") String dni) {
        
        try {
            DniResponse response = dniService.consultarDni(dni);
            return ResponseEntity.ok(response);
        } catch (DniService.DniYaRegistradoException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(null);
        } catch (DniService.FactilizaException e) {
            if (e.getStatusCode() == 404) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(null);
        }
    }
}