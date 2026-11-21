package com.medicalstore.pos.controller;

import com.medicalstore.pos.dto.request.RegistrarBajaRequest;
import com.medicalstore.pos.dto.response.BajaInventarioResponse;
import com.medicalstore.pos.entity.BajaInventario;
import com.medicalstore.pos.repository.UsuarioRepository;
import com.medicalstore.pos.service.BajaInventarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/bajas")
@Tag(name = "M03 Bajas de Inventario", description = "Baja por caducidad, merma o devolucion (RN03, RN06b)")
public class BajaInventarioController extends BaseController {
    
    private final BajaInventarioService bajaInventarioService;
    
    public BajaInventarioController(BajaInventarioService bajaInventarioService,
                                   UsuarioRepository usuarioRepository) {
        super(usuarioRepository);
        this.bajaInventarioService = bajaInventarioService;
    }
    
    @GetMapping
    @Operation(summary = "Listar bajas")
    public ResponseEntity<List<BajaInventarioResponse>> getAll() {
        return ResponseEntity.ok(bajaInventarioService.getAll());
    }
    
    @GetMapping("/rango")
    @Operation(summary = "Bajas en un rango de fechas")
    public ResponseEntity<List<BajaInventarioResponse>> getByRango(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime hasta) {
        return ResponseEntity.ok(bajaInventarioService.getByRango(desde, hasta));
    }
    
    @GetMapping("/motivo/{motivo}")
    @Operation(summary = "Bajas por motivo")
    public ResponseEntity<List<BajaInventarioResponse>> getByMotivo(@PathVariable BajaInventario.Motivo motivo) {
        return ResponseEntity.ok(bajaInventarioService.getByMotivo(motivo));
    }
    
    @GetMapping("/pendientes")
    @Operation(summary = "Lotes vencidos pendientes de baja",
            description = "RN03: el vencido en estante no se puede dispensar, va directo a baja")
    public ResponseEntity<List<BajaInventarioResponse>> pendientes() {
        return ResponseEntity.ok(bajaInventarioService.pendientesDeBaja());
    }
    
    @PostMapping
    @Operation(summary = "Registrar baja de inventario", description = "RN06b: no se elimina el historico")
    public ResponseEntity<BajaInventarioResponse> registrar(
            @Valid @RequestBody RegistrarBajaRequest request, HttpServletRequest httpRequest) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(bajaInventarioService.registrar(request, currentUser(), httpRequest));
    }
}
