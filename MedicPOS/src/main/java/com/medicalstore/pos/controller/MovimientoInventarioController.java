package com.medicalstore.pos.controller;

import com.medicalstore.pos.dto.response.MovimientoInventarioResponse;
import com.medicalstore.pos.entity.MovimientoInventario;
import com.medicalstore.pos.entity.Usuario;
import com.medicalstore.pos.repository.MovimientoInventarioRepository;
import com.medicalstore.pos.repository.UsuarioRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * M03 Kardex de inventario: toda entrada, salida o anula queda registrada.
 */
@RestController
@RequestMapping("/api/movimientos")
@Tag(name = "M03 Movimientos de Inventario", description = "Kardex de entradas, salidas y ajustes")
public class MovimientoInventarioController extends BaseController {
    
    private final MovimientoInventarioRepository movimientoRepository;
    
    public MovimientoInventarioController(MovimientoInventarioRepository movimientoRepository,
                                          UsuarioRepository usuarioRepository) {
        super(usuarioRepository);
        this.movimientoRepository = movimientoRepository;
    }
    
    @GetMapping
    @Operation(summary = "Listar movimientos")
    public ResponseEntity<List<MovimientoInventarioResponse>> getAll() {
        return ResponseEntity.ok(movimientoRepository.findAllDetallado().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList()));
    }
    
    @GetMapping("/lote/{loteId}")
    @Operation(summary = "Movimientos de un lote")
    public ResponseEntity<List<MovimientoInventarioResponse>> getByLote(@PathVariable Long loteId) {
        return ResponseEntity.ok(movimientoRepository.findByLoteId(loteId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList()));
    }
    
    @GetMapping("/referencia")
    @Operation(summary = "Movimientos de una referencia", description = "Filtra por tipo y id, por ejemplo una orden de compra")
    public ResponseEntity<List<MovimientoInventarioResponse>> getByReferencia(
            @RequestParam String tipo, @RequestParam Long id) {
        return ResponseEntity.ok(movimientoRepository.findByReferencia(tipo, id).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList()));
    }
    
    private MovimientoInventarioResponse mapToResponse(MovimientoInventario m) {
        Usuario usuario = m.getUsuario();
        return MovimientoInventarioResponse.builder()
                .id(m.getId())
                .tipoMovimiento(m.getTipoMovimiento())
                .cantidad(m.getCantidad())
                .stockAnterior(m.getStockAnterior())
                .stockNuevo(m.getStockNuevo())
                .referenciaTipo(m.getReferenciaTipo())
                .referenciaId(m.getReferenciaId())
                .fechaHora(m.getFechaHora())
                .medicamentoId(m.getMedicamento().getId())
                .medicamentoNombre(m.getMedicamento().getNombre())
                .loteId(m.getLote() != null ? m.getLote().getId() : null)
                .numeroLote(m.getLote() != null ? m.getLote().getNumeroLote() : null)
                .usuarioNombre(usuario != null ? usuario.getNombreCompleto() : null)
                .build();
    }
}
