package com.medicalstore.pos.controller;

import com.medicalstore.pos.dto.request.ChangePasswordRequest;
import com.medicalstore.pos.dto.request.CreateUsuarioRequest;
import com.medicalstore.pos.dto.request.UpdateUsuarioRequest;
import com.medicalstore.pos.dto.response.UsuarioResponse;
import com.medicalstore.pos.repository.UsuarioRepository;
import com.medicalstore.pos.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
@Tag(name = "M09 Usuarios", description = "Gestion de usuarios del equipo (RN08). Solo Administrador TI")
public class UsuarioController extends BaseController {
    
    private final UsuarioService usuarioService;
    
    public UsuarioController(UsuarioService usuarioService, UsuarioRepository usuarioRepository) {
        super(usuarioRepository);
        this.usuarioService = usuarioService;
    }
    
    @GetMapping
    @Operation(summary = "Listar usuarios")
    public ResponseEntity<List<UsuarioResponse>> getAll() {
        return ResponseEntity.ok(usuarioService.getAll());
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Detalle de un usuario")
    public ResponseEntity<UsuarioResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(usuarioService.getById(id));
    }
    
    @PostMapping
    @Operation(summary = "Registrar usuario")
    public ResponseEntity<UsuarioResponse> create(
            @Valid @RequestBody CreateUsuarioRequest request, HttpServletRequest httpRequest) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(usuarioService.create(request, currentUser(), httpRequest));
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar usuario")
    public ResponseEntity<UsuarioResponse> update(
            @PathVariable Long id, @Valid @RequestBody UpdateUsuarioRequest request,
            HttpServletRequest httpRequest) {
        return ResponseEntity.ok(usuarioService.update(id, request, currentUser(), httpRequest));
    }
    
    @PatchMapping("/{id}/toggle-activo")
    @Operation(summary = "Activar o desactivar usuario", description = "RN08: no se elimina el historial")
    public ResponseEntity<UsuarioResponse> toggleActivo(@PathVariable Long id, HttpServletRequest httpRequest) {
        return ResponseEntity.ok(usuarioService.toggleActivo(id, currentUser(), httpRequest));
    }
    
    @PatchMapping("/{id}/password")
    @Operation(summary = "Cambiar contrasena")
    public ResponseEntity<Void> changePassword(
            @PathVariable Long id, @Valid @RequestBody ChangePasswordRequest request,
            HttpServletRequest httpRequest) {
        usuarioService.changePassword(id, request, currentUser(), httpRequest);
        return ResponseEntity.ok().build();
    }
}
