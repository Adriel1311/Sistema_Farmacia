package com.medicalstore.pos.controller;

import com.medicalstore.pos.dto.request.CreateMedicoRequest;
import com.medicalstore.pos.dto.request.UpdateMedicoRequest;
import com.medicalstore.pos.dto.response.MedicoResponse;
import com.medicalstore.pos.entity.Usuario;
import com.medicalstore.pos.repository.UsuarioRepository;
import com.medicalstore.pos.service.MedicoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/medicos")
@Tag(name = "M12 Medicos", description = "Gestion de medicos para recetas")
public class MedicoController extends BaseController {

    private final MedicoService medicoService;

    public MedicoController(MedicoService medicoService, UsuarioRepository usuarioRepository) {
        super(usuarioRepository);
        this.medicoService = medicoService;
    }

    @GetMapping
    @Operation(summary = "Listar medicos activos")
    public ResponseEntity<List<MedicoResponse>> getAllActivos() {
        return ResponseEntity.ok(medicoService.getAllActivos());
    }

    @GetMapping("/buscar")
    @Operation(summary = "Buscar medicos por nombre")
    public ResponseEntity<List<MedicoResponse>> buscar(@RequestParam String texto) {
        return ResponseEntity.ok(medicoService.buscar(texto));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener medico por ID")
    public ResponseEntity<MedicoResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(medicoService.getById(id));
    }

    @PostMapping
    @Operation(summary = "Crear medico")
    public ResponseEntity<MedicoResponse> create(
            @Valid @RequestBody CreateMedicoRequest request, HttpServletRequest httpRequest) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(medicoService.create(request, currentUser(), httpRequest));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar medico")
    public ResponseEntity<MedicoResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateMedicoRequest request,
            HttpServletRequest httpRequest) {
        return ResponseEntity.ok(medicoService.update(id, request, currentUser(), httpRequest));
    }

    @PatchMapping("/{id}/toggle")
    @Operation(summary = "Activar/Desactivar medico")
    public ResponseEntity<MedicoResponse> toggleActivo(
            @PathVariable Long id,
            HttpServletRequest httpRequest) {
        return ResponseEntity.ok(medicoService.toggleActivo(id, currentUser(), httpRequest));
    }

    @GetMapping("/all")
    @Operation(summary = "Listar todos los medicos (incluye inactivos)")
    public ResponseEntity<List<MedicoResponse>> getAll() {
        return ResponseEntity.ok(medicoService.getAll());
    }
}