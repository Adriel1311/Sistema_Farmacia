package com.medicalstore.pos.controller;

import com.medicalstore.pos.dto.request.AtenderAlertaRequest;
import com.medicalstore.pos.dto.response.AlertaResponse;
import com.medicalstore.pos.repository.UsuarioRepository;
import com.medicalstore.pos.service.AlertaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/alertas")
@Tag(name = "M05 Alertas", description = "Avisos calculados: vencimiento, stock critico y pedido sugerido (RN05)")
public class AlertaController extends BaseController {
    
    private final AlertaService alertaService;
    
    public AlertaController(AlertaService alertaService, UsuarioRepository usuarioRepository) {
        super(usuarioRepository);
        this.alertaService = alertaService;
    }
    
    @GetMapping
    @Operation(summary = "Listar alertas",
            description = "RN05.1: se calculan al abrir la pantalla, no se persisten")
    public ResponseEntity<List<AlertaResponse>> getAll() {
        return ResponseEntity.ok(alertaService.getAll());
    }
    
    @GetMapping("/pendientes")
    @Operation(summary = "Alertas no atendidas", description = "RN05.3: atiende si no hay registro de auditoria")
    public ResponseEntity<List<AlertaResponse>> pendientes() {
        return ResponseEntity.ok(alertaService.getPendientes());
    }
    
    @PostMapping("/atender")
    @Operation(summary = "Marcar alerta como atendida",
            description = "RN05.3: solo escribe en auditoria, no cambia el stock")
    public ResponseEntity<Void> atender(
            @Valid @RequestBody AtenderAlertaRequest request, HttpServletRequest httpRequest) {
        alertaService.atender(request, currentUser(), httpRequest);
        return ResponseEntity.ok().build();
    }
}
