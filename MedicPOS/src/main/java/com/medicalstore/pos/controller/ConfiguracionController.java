package com.medicalstore.pos.controller;

import com.medicalstore.pos.dto.request.UpdateConfiguracionRequest;
import com.medicalstore.pos.dto.response.ConfiguracionResponse;
import com.medicalstore.pos.repository.UsuarioRepository;
import com.medicalstore.pos.service.ConfiguracionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/configuracion")
@Tag(name = "M11 Configuracion", description = "Parametros del sistema y semaforo de vencimiento (RN04). Solo Jefe de Farmacia")
public class ConfiguracionController extends BaseController {
    
    private final ConfiguracionService configuracionService;
    
    public ConfiguracionController(ConfiguracionService configuracionService,
                                   UsuarioRepository usuarioRepository) {
        super(usuarioRepository);
        this.configuracionService = configuracionService;
    }
    
    @GetMapping
    @Operation(summary = "Listar parametros")
    public ResponseEntity<List<ConfiguracionResponse>> getAll() {
        return ResponseEntity.ok(configuracionService.getAll());
    }
    
    @GetMapping("/semaforo")
    @Operation(summary = "Umbrales vigentes", description = "RN04: dias critico y dias proximo a vencer")
    public ResponseEntity<Map<String, Object>> getSemaforo() {
        return ResponseEntity.ok(configuracionService.getSemaforoVigente());
    }

    @GetMapping("/fefo.permitir.excepcion")
    @Operation(summary = "Verificar si se permiten excepciones FEFO", description = "Devuelve true si el Jefe de Farmacia puede forzar un lote distinto al FEFO")
    public ResponseEntity<Map<String, String>> getFefoPermitirExcepcion() {
        boolean permitido = configuracionService.getBoolean(ConfiguracionService.CLAVE_PERMITIR_EXCEPCION_FEFO, true);
        return ResponseEntity.ok(Map.of("valor", String.valueOf(permitido)));
    }
    
    @PutMapping
    @Operation(summary = "Actualizar parametros", description = "Valida que el umbral critico sea menor que el proximo")
    public ResponseEntity<List<ConfiguracionResponse>> update(
            @Valid @RequestBody UpdateConfiguracionRequest request, HttpServletRequest httpRequest) {
        return ResponseEntity.ok(configuracionService.update(request, currentUser(), httpRequest));
    }
}
