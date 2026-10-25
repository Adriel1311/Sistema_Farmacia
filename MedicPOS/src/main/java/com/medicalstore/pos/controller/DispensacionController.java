package com.medicalstore.pos.controller;

import com.medicalstore.pos.dto.request.CreateDispensacionRequest;
import com.medicalstore.pos.dto.response.DispensacionResponse;
import com.medicalstore.pos.repository.UsuarioRepository;
import com.medicalstore.pos.service.DispensacionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dispensaciones")
@Tag(name = "M04 Dispensacion", description = "Venta en caja con FEFO y receta obligatoria (RN02, RN05.0, RN09)")
public class DispensacionController extends BaseController {
    
    private final DispensacionService dispensacionService;
    
    public DispensacionController(DispensacionService dispensacionService, UsuarioRepository usuarioRepository) {
        super(usuarioRepository);
        this.dispensacionService = dispensacionService;
    }
    
    @GetMapping
    @Operation(summary = "Listar dispensaciones")
    public ResponseEntity<List<DispensacionResponse>> getAll() {
        return ResponseEntity.ok(dispensacionService.getAll());
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Detalle de una dispensacion")
    public ResponseEntity<DispensacionResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(dispensacionService.getById(id));
    }
    
    @GetMapping("/receta/{recetaId}")
    @Operation(summary = "Dispensacion de una receta", description = "RN05.0: una receta origina como maximo una dispensacion")
    public ResponseEntity<List<DispensacionResponse>> getByReceta(@PathVariable Long recetaId) {
        return ResponseEntity.ok(dispensacionService.getByReceta(recetaId));
    }
    
    @PostMapping
    @Operation(summary = "Registrar dispensacion",
            description = "RN02: descuenta por FEFO; RN03: nunca despensa lotes vencidos")
    public ResponseEntity<DispensacionResponse> create(
            @Valid @RequestBody CreateDispensacionRequest request, HttpServletRequest httpRequest) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(dispensacionService.create(request, currentUser(), httpRequest));
    }
    
    @PatchMapping("/{id}/anular")
    @Operation(summary = "Anular dispensacion", description = "Devuelve el stock a los lotes de origen y lo audita")
    public ResponseEntity<DispensacionResponse> anular(@PathVariable Long id, HttpServletRequest httpRequest) {
        return ResponseEntity.ok(dispensacionService.anular(id, currentUser(), httpRequest));
    }
}
