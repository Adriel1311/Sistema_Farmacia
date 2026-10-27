package com.medicalstore.pos.controller;

import com.medicalstore.pos.dto.request.CreateRecetaRequest;
import com.medicalstore.pos.dto.response.RecetaResponse;
import com.medicalstore.pos.entity.Receta;
import com.medicalstore.pos.repository.UsuarioRepository;
import com.medicalstore.pos.service.RecetaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/recetas")
@Tag(name = "M04 Recetas", description = "Recetas medicas obligatorias (RN05.0)")
public class RecetaController extends BaseController {
    
    private final RecetaService recetaService;
    
    public RecetaController(RecetaService recetaService, UsuarioRepository usuarioRepository) {
        super(usuarioRepository);
        this.recetaService = recetaService;
    }
    
    @GetMapping
    @Operation(summary = "Listar recetas", description = "Filtros opcionales por tipo, rango de fecha y pendientes")
    public ResponseEntity<List<RecetaResponse>> getAll(
            @RequestParam(required = false) Receta.TipoReceta tipo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(required = false) Boolean pendiente) {
        return ResponseEntity.ok(recetaService.getAll(tipo, desde, hasta, pendiente));
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Detalle de una receta")
    public ResponseEntity<RecetaResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(recetaService.getById(id));
    }
    
    @GetMapping("/proximo-numero")
    @Operation(summary = "Obtener proximo numero de receta", description = "Genera el siguiente numero de receta auto-incremental con formato REC-YYYY-NNNNN")
    public ResponseEntity<Map<String, String>> getProximoNumeroReceta() {
        String proximo = recetaService.generarSiguienteNumeroReceta();
        return ResponseEntity.ok(Map.of("valor", proximo));
    }
    
    @PostMapping
    @Operation(summary = "Registrar receta", description = "RN05.0: una receta origina como maximo una dispensacion")
    public ResponseEntity<RecetaResponse> create(
            @Valid @RequestBody CreateRecetaRequest request, HttpServletRequest httpRequest) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(recetaService.create(request, currentUser(), httpRequest));
    }
}
