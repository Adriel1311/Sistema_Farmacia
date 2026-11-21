package com.medicalstore.pos.controller;

import com.medicalstore.pos.dto.request.CreateMedicamentoPrecioRequest;
import com.medicalstore.pos.dto.request.UpdateMedicamentoPrecioRequest;
import com.medicalstore.pos.dto.response.MedicamentoPrecioResponse;
import com.medicalstore.pos.service.MedicamentoPrecioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/precios")
@Tag(name = "M12 Precios", description = "Gestión de precios de venta de medicamentos")
public class PrecioController extends BaseController {

    private final MedicamentoPrecioService precioService;

    public PrecioController(MedicamentoPrecioService precioService, 
                            com.medicalstore.pos.repository.UsuarioRepository usuarioRepository) {
        super(usuarioRepository);
        this.precioService = precioService;
    }

    @GetMapping
    @Operation(summary = "Listar todos los precios")
    public ResponseEntity<List<MedicamentoPrecioResponse>> getAll() {
        return ResponseEntity.ok(precioService.getAll());
    }

    @GetMapping("/medicamento/{medicamentoId}")
    @Operation(summary = "Listar precios por medicamento")
    public ResponseEntity<List<MedicamentoPrecioResponse>> getByMedicamento(@PathVariable Long medicamentoId) {
        return ResponseEntity.ok(precioService.getByMedicamento(medicamentoId));
    }

    @PostMapping
    @Operation(summary = "Crear precio")
    public ResponseEntity<MedicamentoPrecioResponse> crear(
            @Valid @RequestBody CreateMedicamentoPrecioRequest request,
            HttpServletRequest httpRequest) {
        return ResponseEntity.ok(precioService.crear(request, currentUser(), httpRequest));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar precio")
    public ResponseEntity<MedicamentoPrecioResponse> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody UpdateMedicamentoPrecioRequest request,
            HttpServletRequest httpRequest) {
        return ResponseEntity.ok(precioService.actualizar(id, request, currentUser(), httpRequest));
    }
}