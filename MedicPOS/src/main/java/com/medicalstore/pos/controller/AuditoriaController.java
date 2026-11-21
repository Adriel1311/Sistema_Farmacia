package com.medicalstore.pos.controller;

import com.medicalstore.pos.dto.response.AuditoriaResponse;
import com.medicalstore.pos.repository.UsuarioRepository;
import com.medicalstore.pos.service.AuditoriaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/auditoria")
@Tag(name = "M15 Auditoria", description = "Traza de operaciones (RN08). Solo lectura")
public class AuditoriaController extends BaseController {
    
    private final AuditoriaService auditoriaService;
    
    public AuditoriaController(AuditoriaService auditoriaService, UsuarioRepository usuarioRepository) {
        super(usuarioRepository);
        this.auditoriaService = auditoriaService;
    }
    
    @GetMapping
    @Operation(summary = "Listar auditoria")
    public ResponseEntity<List<AuditoriaResponse>> getAll() {
        return ResponseEntity.ok(auditoriaService.getAll());
    }
    
    @GetMapping("/entidad/{entidad}")
    @Operation(summary = "Auditoria de una entidad")
    public ResponseEntity<List<AuditoriaResponse>> getByEntidad(@PathVariable String entidad) {
        return ResponseEntity.ok(auditoriaService.getByEntidad(entidad));
    }
    
    @GetMapping("/usuario/{usuarioId}")
    @Operation(summary = "Auditoria de un usuario")
    public ResponseEntity<List<AuditoriaResponse>> getByUsuario(@PathVariable Long usuarioId) {
        return ResponseEntity.ok(auditoriaService.getByUsuario(usuarioId));
    }
    
    @GetMapping("/rango")
    @Operation(summary = "Auditoria en un rango de fechas")
    public ResponseEntity<List<AuditoriaResponse>> getByRango(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime hasta) {
        return ResponseEntity.ok(auditoriaService.getByRangoFechas(desde, hasta));
    }
}
