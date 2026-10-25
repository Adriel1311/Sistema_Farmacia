package com.medicalstore.pos.controller;

import com.medicalstore.pos.dto.request.CreateProveedorRequest;
import com.medicalstore.pos.dto.request.UpdateProveedorRequest;
import com.medicalstore.pos.dto.response.ProveedorResponse;
import com.medicalstore.pos.repository.UsuarioRepository;
import com.medicalstore.pos.service.ProveedorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/proveedores")
@Tag(name = "M06 Proveedores", description = "Catalogo de proveedores (RN07)")
public class ProveedorController extends BaseController {
    
    private final ProveedorService proveedorService;
    
    public ProveedorController(ProveedorService proveedorService, UsuarioRepository usuarioRepository) {
        super(usuarioRepository);
        this.proveedorService = proveedorService;
    }
    
    @GetMapping
    @Operation(summary = "Listar proveedores")
    public ResponseEntity<List<ProveedorResponse>> getAll(
            @RequestParam(defaultValue = "true") boolean soloActivos) {
        return ResponseEntity.ok(proveedorService.getAll(soloActivos));
    }
    
    @GetMapping("/buscar")
    @Operation(summary = "Buscar proveedor por RUC o razon social")
    public ResponseEntity<List<ProveedorResponse>> buscar(@RequestParam String texto) {
        return ResponseEntity.ok(proveedorService.buscar(texto));
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Detalle de un proveedor")
    public ResponseEntity<ProveedorResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(proveedorService.getById(id));
    }
    
    @PostMapping
    @Operation(summary = "Registrar proveedor", description = "Solo Logistica y Jefe de Farmacia")
    public ResponseEntity<ProveedorResponse> create(
            @Valid @RequestBody CreateProveedorRequest request, HttpServletRequest httpRequest) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(proveedorService.create(request, currentUser(), httpRequest));
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar proveedor")
    public ResponseEntity<ProveedorResponse> update(
            @PathVariable Long id, @Valid @RequestBody UpdateProveedorRequest request,
            HttpServletRequest httpRequest) {
        return ResponseEntity.ok(proveedorService.update(id, request, currentUser(), httpRequest));
    }
    
    @PatchMapping("/{id}/toggle-activo")
    @Operation(summary = "Activar o desactivar proveedor", description = "RN07: no se elimina, se desactiva")
    public ResponseEntity<ProveedorResponse> toggleActivo(@PathVariable Long id, HttpServletRequest httpRequest) {
        return ResponseEntity.ok(proveedorService.toggleActivo(id, currentUser(), httpRequest));
    }
}
