package com.medicalstore.pos.service;

import com.medicalstore.pos.dto.request.RegistrarBajaRequest;
import com.medicalstore.pos.dto.response.BajaInventarioResponse;
import com.medicalstore.pos.entity.Auditoria;
import com.medicalstore.pos.entity.BajaInventario;
import com.medicalstore.pos.entity.Lote;
import com.medicalstore.pos.entity.MovimientoInventario;
import com.medicalstore.pos.entity.Usuario;
import com.medicalstore.pos.repository.BajaInventarioRepository;
import com.medicalstore.pos.repository.LoteRepository;
import com.medicalstore.pos.repository.MovimientoInventarioRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * M03 Inventario - baja de existencias (RF-14, RN06b, RN03).
 *
 * La baja nunca elimina el lote ni sus movimientos: solo descuenta existencias
 * y deja constancia de quien lo hizo y por que (RN08).
 */
@Service
public class BajaInventarioService {
    
    private final BajaInventarioRepository bajaRepository;
    private final LoteRepository loteRepository;
    private final MovimientoInventarioRepository movimientoRepository;
    private final AuditoriaService auditoriaService;
    
    public BajaInventarioService(BajaInventarioRepository bajaRepository,
                                 LoteRepository loteRepository,
                                 MovimientoInventarioRepository movimientoRepository,
                                 AuditoriaService auditoriaService) {
        this.bajaRepository = bajaRepository;
        this.loteRepository = loteRepository;
        this.movimientoRepository = movimientoRepository;
        this.auditoriaService = auditoriaService;
    }
    
    @Transactional(rollbackFor = Exception.class)
    public BajaInventarioResponse registrar(RegistrarBajaRequest request, Usuario user, HttpServletRequest httpRequest) {
        Lote lote = loteRepository.findByIdParaActualizar(request.getLoteId())
                .orElseThrow(() -> new RuntimeException("Lote not found with id: " + request.getLoteId()));
        
        if (lote.getEstado() == Lote.Estado.BAJA) {
            throw new RuntimeException("El lote " + lote.getNumeroLote() + " ya fue dado de baja");
        }
        if (request.getCantidad() > lote.getCantidadActual()) {
            throw new RuntimeException("No se puede dar de baja " + request.getCantidad() 
                    + " unidades, el lote " + lote.getNumeroLote() + " solo tiene " + lote.getCantidadActual());
        }
        if (request.getMotivo() == BajaInventario.Motivo.VENCIDO_EN_ESTANTE && !lote.isVencido()) {
            throw new RuntimeException("El lote " + lote.getNumeroLote() + " aun no esta vencido, "
                    + "use otro motivo de baja");
        }
        if (request.getMotivo() != BajaInventario.Motivo.VENCIDO_EN_ESTANTE && lote.isVencido()) {
            throw new RuntimeException("El lote " + lote.getNumeroLote() + " esta vencido, "
                    + "el motivo debe ser VENCIDO_EN_ESTANTE");
        }
        
        int stockAnterior = lote.getCantidadActual();
        int stockNuevo = stockAnterior - request.getCantidad();
        lote.setCantidadActual(stockNuevo);
        lote.setUpdatedBy(user != null ? user.getId() : null);
        // RN06b: el lote queda marcado como baja cuando ya no tiene existencias
        if (stockNuevo == 0) {
            lote.setEstado(Lote.Estado.BAJA);
        }
        loteRepository.save(lote);
        
        BajaInventario baja = BajaInventario.builder()
                .lote(lote)
                .usuario(user)
                .motivo(request.getMotivo())
                .cantidad(request.getCantidad())
                .observacion(request.getObservacion())
                .valorPerdida(valorPerdida(lote, request.getCantidad()))
                .build();
        baja = bajaRepository.save(baja);
        
        MovimientoInventario movimiento = MovimientoInventario.builder()
                .medicamento(lote.getMedicamento())
                .lote(lote)
                .usuario(user)
                .tipoMovimiento(MovimientoInventario.TipoMovimiento.BAJA)
                .cantidad(request.getCantidad())
                .stockAnterior(stockAnterior)
                .stockNuevo(stockNuevo)
                .referenciaTipo("BAJA_INVENTARIO")
                .referenciaId(baja.getId())
                .build();
        movimientoRepository.save(movimiento);
        
        auditoriaService.log(Auditoria.Accion.BAJA_REGISTRADA, user, "Lote", lote.getId().toString(),
                "Baja de " + request.getCantidad() + " unidades del lote " + lote.getNumeroLote() 
                        + " (" + request.getMotivo() + ")",
                String.valueOf(stockAnterior), String.valueOf(stockNuevo), httpRequest);
        
        return mapToResponse(baja);
    }
    
    @Transactional(readOnly = true)
    public List<BajaInventarioResponse> getAll() {
        return bajaRepository.findAllDetallado().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<BajaInventarioResponse> getByRango(LocalDateTime desde, LocalDateTime hasta) {
        return bajaRepository.findByRangoFechas(desde, hasta).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<BajaInventarioResponse> getByMotivo(BajaInventario.Motivo motivo) {
        return bajaRepository.findByMotivo(motivo).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public long countByRango(LocalDateTime desde, LocalDateTime hasta) {
        return bajaRepository.countByFechaHoraBetween(desde, hasta);
    }
    
    /**
     * RN03: vencidos en el estante que aun tienen unidades por dar de baja.
     */
    @Transactional(readOnly = true)
    public List<BajaInventarioResponse> pendientesDeBaja() {
        LocalDate hoy = LocalDate.now();
        return loteRepository.findVencidosConStock(hoy).stream()
                .map(l -> BajaInventarioResponse.builder()
                        .loteId(l.getId())
                        .numeroLote(l.getNumeroLote())
                        .fechaVencimientoLote(l.getFechaVencimiento())
                        .medicamentoId(l.getMedicamento().getId())
                        .medicamentoCodigo(l.getMedicamento().getCodigo())
                        .medicamentoNombre(l.getMedicamento().getNombre())
                        .motivo(BajaInventario.Motivo.VENCIDO_EN_ESTANTE)
                        .build())
                .collect(Collectors.toList());
    }
    
    /**
     * El valor de la perdida se calcula en el servidor con el costo de adquisicion
     * del medicamento, nunca lo manda el cliente. Mide el costo de la merma:
     * el sistema no cobra nada por la baja.
     */
    private BigDecimal valorPerdida(Lote lote, int cantidad) {
        BigDecimal costo = lote.getMedicamento().getCostoUnitario();
        if (costo == null) {
            return BigDecimal.ZERO;
        }
        return costo.multiply(BigDecimal.valueOf(cantidad));
    }
    
    private BajaInventarioResponse mapToResponse(BajaInventario b) {
        Lote lote = b.getLote();
        Usuario usuario = b.getUsuario();
        return BajaInventarioResponse.builder()
                .id(b.getId())
                .motivo(b.getMotivo())
                .cantidad(b.getCantidad())
                .observacion(b.getObservacion())
                .valorPerdida(b.getValorPerdida())
                .fechaHora(b.getFechaHora())
                .loteId(lote.getId())
                .numeroLote(lote.getNumeroLote())
                .fechaVencimientoLote(lote.getFechaVencimiento())
                .medicamentoId(lote.getMedicamento().getId())
                .medicamentoCodigo(lote.getMedicamento().getCodigo())
                .medicamentoNombre(lote.getMedicamento().getNombre())
                .usuarioId(usuario != null ? usuario.getId() : null)
                .usuarioNombre(usuario != null ? usuario.getNombreCompleto() : null)
                .usuarioRol(usuario != null ? usuario.getNombreRol() : null)
                .build();
    }
}
