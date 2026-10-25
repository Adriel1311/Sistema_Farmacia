package com.medicalstore.pos.controller;

import com.medicalstore.pos.dto.request.CreateMedicamentoRequest;
import com.medicalstore.pos.dto.request.UpdateMedicamentoRequest;
import com.medicalstore.pos.dto.response.MedicamentoResponse;
import com.medicalstore.pos.repository.UsuarioRepository;
import com.medicalstore.pos.service.MedicamentoService;
import com.medicalstore.pos.service.StockService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/medicamentos")
@Tag(name = "M02 Medicamentos", description = "Catalogo de medicamentos y stock vigente (RN06, RN07)")
public class MedicamentoController extends BaseController {
    
    private final MedicamentoService medicamentoService;
    private final StockService stockService;
    
    public MedicamentoController(MedicamentoService medicamentoService,
                                StockService stockService,
                                UsuarioRepository usuarioRepository) {
        super(usuarioRepository);
        this.medicamentoService = medicamentoService;
        this.stockService = stockService;
    }
    
    @GetMapping
    @Operation(summary = "Listar medicamentos")
    public ResponseEntity<List<MedicamentoResponse>> getAll(
            @RequestParam(defaultValue = "true") boolean soloActivos) {
        return ResponseEntity.ok(medicamentoService.getAll(soloActivos));
    }
    
    @GetMapping("/buscar")
    @Operation(summary = "Buscar medicamentos por nombre o codigo")
    public ResponseEntity<List<MedicamentoResponse>> buscar(@RequestParam String texto) {
        return ResponseEntity.ok(medicamentoService.buscar(texto));
    }
    
    @GetMapping("/proveedor/{proveedorId}")
    @Operation(summary = "Medicamentos de un proveedor")
    public ResponseEntity<List<MedicamentoResponse>> getByProveedor(@PathVariable Long proveedorId) {
        return ResponseEntity.ok(medicamentoService.getByProveedor(proveedorId));
    }
    
    @GetMapping("/stock-critico")
    @Operation(summary = "Medicamentos en stock critico", description = "RN06: igualo o quedo por debajo del minimo")
    public ResponseEntity<List<MedicamentoResponse>> stockCritico() {
        LocalDate hoy = LocalDate.now();
        List<MedicamentoResponse> todos = medicamentoService.getAll(true);
        List<MedicamentoResponse> criticos = todos.stream()
                .filter(m -> m.getStockMinimo() != null
                        && stockService.getStockVigente(m.getId(), hoy) <= m.getStockMinimo())
                .collect(java.util.stream.Collectors.toList());
        return ResponseEntity.ok(criticos);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Detalle de un medicamento")
    public ResponseEntity<MedicamentoResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(medicamentoService.getById(id));
    }
    
    @PostMapping
    @Operation(summary = "Registrar medicamento", description = "Solo Jefe de Farmacia (RN07)")
    public ResponseEntity<MedicamentoResponse> create(
            @Valid @RequestBody CreateMedicamentoRequest request, HttpServletRequest httpRequest) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(medicamentoService.create(request, currentUser(), httpRequest));
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar medicamento")
    public ResponseEntity<MedicamentoResponse> update(
            @PathVariable Long id, @Valid @RequestBody UpdateMedicamentoRequest request,
            HttpServletRequest httpRequest) {
        return ResponseEntity.ok(medicamentoService.update(id, request, currentUser(), httpRequest));
    }
    
    @PatchMapping("/{id}/toggle-activo")
    @Operation(summary = "Activar o desactivar medicamento")
    public ResponseEntity<MedicamentoResponse> toggleActivo(@PathVariable Long id, HttpServletRequest httpRequest) {
        return ResponseEntity.ok(medicamentoService.toggleActivo(id, currentUser(), httpRequest));
    }
}
