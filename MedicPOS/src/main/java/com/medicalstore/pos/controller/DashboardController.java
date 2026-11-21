package com.medicalstore.pos.controller;

import com.medicalstore.pos.dto.response.DashboardResponse;
import com.medicalstore.pos.repository.UsuarioRepository;
import com.medicalstore.pos.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@Tag(name = "M07 Dashboard", description = "Indicadores consolidados del panel (RF-13)")
public class DashboardController extends BaseController {
    
    private final DashboardService dashboardService;
    
    public DashboardController(DashboardService dashboardService, UsuarioRepository usuarioRepository) {
        super(usuarioRepository);
        this.dashboardService = dashboardService;
    }
    
    @GetMapping
    @Operation(summary = "Panel operativo",
            description = "Semaforo de vencimientos, stock critico y actividad del dia")
    public ResponseEntity<DashboardResponse> getDashboard() {
        return ResponseEntity.ok(dashboardService.getDashboard());
    }
}
