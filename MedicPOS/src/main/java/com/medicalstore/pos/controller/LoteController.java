package com.medicalstore.pos.controller;

import com.medicalstore.pos.dto.request.RegistrarLoteRequest;
import com.medicalstore.pos.dto.response.LoteResponse;
import com.medicalstore.pos.entity.Lote;
import com.medicalstore.pos.repository.UsuarioRepository;
import com.medicalstore.pos.service.LoteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/lotes")
@Tag(name = "M03 Inventario y Lotes", description = "Ingreso por lotes, semaforo y vencimientos (RN01, RN03, RN04)")
public class LoteController extends BaseController {
    
    private final LoteService loteService;
    
    public LoteController(LoteService loteService, UsuarioRepository usuarioRepository) {
        super(usuarioRepository);
        this.loteService = loteService;
    }
    
    @GetMapping
    @Operation(summary = "Listar lotes", description = "Filtros opcionales por medicamento y estado")
    public ResponseEntity<List<LoteResponse>> getAll(
            @RequestParam(required = false) Long medicamentoId,
            @RequestParam(required = false) Lote.Estado estado) {
        return ResponseEntity.ok(loteService.getAll(medicamentoId, estado));
    }
    
    @GetMapping("/vencidos")
    @Operation(summary = "Lotes vencidos con existencias", description = "RN03: no se pueden dispensar, van a baja")
    public ResponseEntity<List<LoteResponse>> vencidos() {
        return ResponseEntity.ok(loteService.getVencidos());
    }
    
    @GetMapping("/proximos-vencer")
    @Operation(summary = "Lotes dentro del semaforo amber", description = "RN04: entre el umbral critico y el de proximo a vencer")
    public ResponseEntity<List<LoteResponse>> proximosVencer() {
        return ResponseEntity.ok(loteService.getProximosAVencer());
    }
    
    @GetMapping("/criticos")
    @Operation(summary = "Lotes en rojo", description = "RN04: vencen dentro del umbral critico")
    public ResponseEntity<List<LoteResponse>> criticos() {
        return ResponseEntity.ok(loteService.getCriticos());
    }
    
    @GetMapping("/medicamento/{medicamentoId}")
    @Operation(summary = "Lotes de un medicamento")
    public ResponseEntity<List<LoteResponse>> getByMedicamento(@PathVariable Long medicamentoId) {
        return ResponseEntity.ok(loteService.getByMedicamento(medicamentoId));
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Detalle de un lote")
    public ResponseEntity<LoteResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(loteService.getById(id));
    }
    
    @PostMapping
    @Operation(summary = "Registrar ingreso de lote", description = "RN01: todo medicamento entra por lote con vencimiento")
    public ResponseEntity<LoteResponse> registrarIngreso(
            @Valid @RequestBody RegistrarLoteRequest request, HttpServletRequest httpRequest) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(loteService.registrarIngreso(request, currentUser(), httpRequest));
    }
    
    @PostMapping("/sincronizar-vencidos")
    @Operation(summary = "Sincronizar lotes vencidos", description = "Marca VENCIDO los lotes cuya fecha ya paso")
    public ResponseEntity<Integer> sincronizarVencidos() {
        return ResponseEntity.ok(loteService.sincronizarVencidos());
    }
}
