package com.medicalstore.pos.controller;

import com.medicalstore.pos.dto.request.AbrirCajaRequest;
import com.medicalstore.pos.dto.request.CerrarCajaRequest;
import com.medicalstore.pos.dto.request.SalidaCajaRequest;
import com.medicalstore.pos.dto.response.CajaResponse;
import com.medicalstore.pos.dto.response.MovimientoCajaResponse;
import com.medicalstore.pos.repository.UsuarioRepository;
import com.medicalstore.pos.service.CajaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/caja")
@Tag(name = "M11 Caja", description = "Apertura con monto inicial, salidas, ventas por boleta y cierre con arqueo")
public class CajaController extends BaseController {

    private final CajaService cajaService;

    public CajaController(CajaService cajaService, UsuarioRepository usuarioRepository) {
        super(usuarioRepository);
        this.cajaService = cajaService;
    }

    @GetMapping("/actual")
    @Operation(summary = "Caja abierta", description = "204 si no hay ninguna caja abierta")
    public ResponseEntity<CajaResponse> actual() {
        return cajaService.getActual()
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @GetMapping("/historial")
    @Operation(summary = "Cajas recientes (abiertas y cerradas)")
    public ResponseEntity<List<CajaResponse>> historial() {
        return ResponseEntity.ok(cajaService.getHistorial());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Resumen de una caja")
    public ResponseEntity<CajaResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(cajaService.getById(id));
    }

    @GetMapping("/{id}/movimientos")
    @Operation(summary = "Movimientos de una caja", description = "Incluye el detalle de cada boleta vendida")
    public ResponseEntity<List<MovimientoCajaResponse>> movimientos(@PathVariable Long id) {
        return ResponseEntity.ok(cajaService.getMovimientos(id));
    }

    @PostMapping("/abrir")
    @Operation(summary = "Abrir caja con monto inicial")
    public ResponseEntity<CajaResponse> abrir(@Valid @RequestBody AbrirCajaRequest request, HttpServletRequest httpRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cajaService.abrir(request, currentUser(), httpRequest));
    }

    @PostMapping("/salidas")
    @Operation(summary = "Registrar salida (retiro de dinero)")
    public ResponseEntity<CajaResponse> salida(@Valid @RequestBody SalidaCajaRequest request, HttpServletRequest httpRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cajaService.registrarSalida(request, currentUser(), httpRequest));
    }

    @PostMapping("/cerrar")
    @Operation(summary = "Cerrar caja con arqueo")
    public ResponseEntity<CajaResponse> cerrar(@Valid @RequestBody CerrarCajaRequest request, HttpServletRequest httpRequest) {
        return ResponseEntity.ok(cajaService.cerrar(request, currentUser(), httpRequest));
    }
}
