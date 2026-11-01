package com.medicalstore.pos.controller;

import com.medicalstore.pos.dto.request.PagarCobroRequest;
import com.medicalstore.pos.dto.response.CobroResponse;
import com.medicalstore.pos.service.CobroService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/cobros")
@Tag(name = "Cobros", description = "Gestión de cobros y pagos de dispensaciones")
public class CobroController extends BaseController {

    private final CobroService cobroService;

    public CobroController(CobroService cobroService, 
                           com.medicalstore.pos.repository.UsuarioRepository usuarioRepository) {
        super(usuarioRepository);
        this.cobroService = cobroService;
    }

    @GetMapping
    @Operation(summary = "Listar todos los cobros")
    public ResponseEntity<List<CobroResponse>> getAll() {
        return ResponseEntity.ok(cobroService.getAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener cobro por ID")
    public ResponseEntity<CobroResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(cobroService.getById(id));
    }

    @GetMapping("/dispensacion/{dispensacionId}")
    @Operation(summary = "Obtener cobro por dispensación")
    public ResponseEntity<CobroResponse> getByDispensacion(@PathVariable Long dispensacionId) {
        return cobroService.getByDispensacionId(dispensacionId)
                .map(cobro -> ResponseEntity.ok(cobro))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/pagar")
    @Operation(summary = "Registrar pago de cobro")
    public ResponseEntity<CobroResponse> pagar(
            @PathVariable Long id,
            @Valid @RequestBody PagarCobroRequest request,
            HttpServletRequest httpRequest) {
        return ResponseEntity.ok(cobroService.pagar(id, request, currentUser(), httpRequest));
    }

    @GetMapping("/rango")
    @Operation(summary = "Listar cobros por rango de fechas")
    public ResponseEntity<List<CobroResponse>> getByRango(
            @RequestParam LocalDateTime desde,
            @RequestParam LocalDateTime hasta) {
        return ResponseEntity.ok(cobroService.getByRangoFechas(desde, hasta));
    }
}