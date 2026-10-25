package com.medicalstore.pos.controller;

import com.medicalstore.pos.dto.request.CrearDesdeSugerenciaRequest;
import com.medicalstore.pos.dto.request.CreateOrdenCompraRequest;
import com.medicalstore.pos.dto.request.ResolverOrdenCompraRequest;
import com.medicalstore.pos.dto.response.OrdenCompraResponse;
import com.medicalstore.pos.dto.response.SugerenciaCompraResponse;
import com.medicalstore.pos.entity.OrdenCompra;
import com.medicalstore.pos.repository.UsuarioRepository;
import com.medicalstore.pos.service.OrdenCompraService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ordenes")
@Tag(name = "M06 Ordenes de Compra", description = "Pedidos a proveedores y pedido sugerido (RN07, RF-09, RF-10A)")
public class OrdenCompraController extends BaseController {
    
    private final OrdenCompraService ordenCompraService;
    
    public OrdenCompraController(OrdenCompraService ordenCompraService, UsuarioRepository usuarioRepository) {
        super(usuarioRepository);
        this.ordenCompraService = ordenCompraService;
    }
    
    @GetMapping
    @Operation(summary = "Listar ordenes de compra")
    public ResponseEntity<List<OrdenCompraResponse>> getAll(
            @RequestParam(required = false) OrdenCompra.Estado estado,
            @RequestParam(required = false) Long proveedorId) {
        return ResponseEntity.ok(ordenCompraService.getAll(estado, proveedorId));
    }
    
    @GetMapping("/sugerencias")
    @Operation(summary = "Pedido sugerido", description = "RF-09: medicamentos en stock critico con cantidad propuesta")
    public ResponseEntity<List<SugerenciaCompraResponse>> getSugerencias() {
        return ResponseEntity.ok(ordenCompraService.getSugerencias());
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Detalle de una orden")
    public ResponseEntity<OrdenCompraResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ordenCompraService.getById(id));
    }
    
    @PostMapping
    @Operation(summary = "Crear orden de compra", description = "RF-10A: Logistica crea, el Jefe aprueba o rechaza")
    public ResponseEntity<OrdenCompraResponse> create(
            @Valid @RequestBody CreateOrdenCompraRequest request, HttpServletRequest httpRequest) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ordenCompraService.create(request, currentUser(), httpRequest));
    }
    
    @PostMapping("/desde-sugerencia")
    @Operation(summary = "Convertir sugerencias en orden",
            description = "RN07: una orden abarca un solo proveedor")
    public ResponseEntity<OrdenCompraResponse> desdeSugerencia(
            @Valid @RequestBody CrearDesdeSugerenciaRequest request, HttpServletRequest httpRequest) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ordenCompraService.crearDesdeSugerencia(
                        request.getSugerencias(), currentUser(), request.getObservacion(), httpRequest));
    }
    
    @PatchMapping("/{id}/resolver")
    @Operation(summary = "Aprobar o rechazar una orden", description = "Solo Jefe de Farmacia")
    public ResponseEntity<OrdenCompraResponse> resolver(
            @PathVariable Long id, @Valid @RequestBody ResolverOrdenCompraRequest request,
            HttpServletRequest httpRequest) {
        return ResponseEntity.ok(ordenCompraService.resolver(id, request, currentUser(), httpRequest));
    }
}
