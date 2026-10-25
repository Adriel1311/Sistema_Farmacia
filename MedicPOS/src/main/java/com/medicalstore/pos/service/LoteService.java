package com.medicalstore.pos.service;

import com.medicalstore.pos.dto.request.RegistrarLoteRequest;
import com.medicalstore.pos.dto.response.LoteResponse;
import com.medicalstore.pos.entity.Auditoria;
import com.medicalstore.pos.entity.Lote;
import com.medicalstore.pos.entity.Medicamento;
import com.medicalstore.pos.entity.MovimientoInventario;
import com.medicalstore.pos.entity.Usuario;
import com.medicalstore.pos.repository.LoteRepository;
import com.medicalstore.pos.repository.MedicamentoRepository;
import com.medicalstore.pos.repository.MovimientoInventarioRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * M03 Inventario y Lotes (RF-02, RF-03, RF-05, RN01, RN01b, RN01c, RN04).
 */
@Service
public class LoteService {
    
    private final LoteRepository loteRepository;
    private final MedicamentoRepository medicamentoRepository;
    private final MovimientoInventarioRepository movimientoRepository;
    private final ConfiguracionService configuracionService;
    private final AuditoriaService auditoriaService;
    
    public LoteService(LoteRepository loteRepository,
                       MedicamentoRepository medicamentoRepository,
                       MovimientoInventarioRepository movimientoRepository,
                       ConfiguracionService configuracionService,
                       AuditoriaService auditoriaService) {
        this.loteRepository = loteRepository;
        this.medicamentoRepository = medicamentoRepository;
        this.movimientoRepository = movimientoRepository;
        this.configuracionService = configuracionService;
        this.auditoriaService = auditoriaService;
    }
    
    /**
     * RF-05: registra el ingreso de un lote con su fecha de vencimiento (RN01).
     */
    @Transactional
    public LoteResponse registrarIngreso(RegistrarLoteRequest request, Usuario user, HttpServletRequest httpRequest) {
        Medicamento medicamento = medicamentoRepository.findByIdWithProveedor(request.getMedicamentoId())
                .orElseThrow(() -> new RuntimeException("Medicamento not found with id: " + request.getMedicamentoId()));
        
        if (!medicamento.getActivo()) {
            throw new RuntimeException("No se puede registrar lotes de un medicamento inactivo: " + medicamento.getNombre());
        }
        
        // RN01b: un lote es de un solo medicamento y no se repite
        if (loteRepository.existsByMedicamentoIdAndNumeroLote(medicamento.getId(), request.getNumeroLote())) {
            throw new RuntimeException("El lote " + request.getNumeroLote() + " ya esta registrado para " + medicamento.getNombre());
        }
        
        // RN01c: todo lote entra valido y en positivo
        if (!request.getFechaVencimiento().isAfter(request.getFechaIngreso())) {
            throw new RuntimeException("La fecha de vencimiento debe ser posterior a la fecha de ingreso");
        }
        if (request.getCantidad() == null || request.getCantidad() <= 0) {
            throw new RuntimeException("La cantidad debe ser mayor a cero");
        }
        
        Lote lote = Lote.builder()
                .medicamento(medicamento)
                .numeroLote(request.getNumeroLote())
                .fechaIngreso(request.getFechaIngreso())
                .fechaVencimiento(request.getFechaVencimiento())
                .cantidadInicial(request.getCantidad())
                .cantidadActual(request.getCantidad())
                .estado(Lote.Estado.ACTIVO)
                .createdBy(user != null ? user.getId() : null)
                .build();
        
        lote = loteRepository.save(lote);
        
        // Kardex de inventario
        registrarMovimiento(lote, user, MovimientoInventario.TipoMovimiento.ENTRADA,
                request.getCantidad(), 0, request.getCantidad(),
                request.getOrdenCompraId() != null ? "ORDEN_COMPRA" : "INGRESO_LOTE",
                request.getOrdenCompraId() != null ? request.getOrdenCompraId() : lote.getId());
        
        auditoriaService.log(Auditoria.Accion.LOTE_INGRESADO, user, "Lote", lote.getId().toString(),
                "Ingreso de lote " + lote.getNumeroLote() + " de " + medicamento.getNombre()
                        + " (" + request.getCantidad() + " " + medicamento.getUnidadMedida() + ")",
                null, lote.toString(), httpRequest);
        
        return mapToResponse(lote);
    }
    
    @Transactional(readOnly = true)
    public LoteResponse getById(Long id) {
        return mapToResponse(obtenerEntidad(id));
    }
    
    @Transactional(readOnly = true)
    public List<LoteResponse> getAll(Long medicamentoId, Lote.Estado estado) {
        List<Lote> lotes;
        if (medicamentoId != null && estado != null) {
            lotes = loteRepository.findByMedicamentoIdAndEstadoOrderByVencimiento(medicamentoId, estado);
        } else if (medicamentoId != null) {
            lotes = loteRepository.findByMedicamentoIdOrdenado(medicamentoId);
        } else if (estado != null) {
            lotes = loteRepository.findByEstadoOrderByVencimiento(estado);
        } else {
            lotes = loteRepository.findAllWithMedicamentoOrderByVencimiento();
        }
        return lotes.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<LoteResponse> getByMedicamento(Long medicamentoId) {
        return loteRepository.findByMedicamentoIdWithMedicamento(medicamentoId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public Lote obtenerEntidad(Long id) {
        return loteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Lote not found with id: " + id));
    }
    
    /**
     * Lotes vencidos que aun tienen existencias fisicas (RN03).
     */
    @Transactional(readOnly = true)
    public List<LoteResponse> getVencidos() {
        return loteRepository.findVencidosConStock(LocalDate.now()).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Lotes que vencen dentro de la ventana del semaforo (RF-03).
     */
    @Transactional(readOnly = true)
    public List<LoteResponse> getProximosAVencer() {
        LocalDate hoy = LocalDate.now();
        int dias = configuracionService.getDiasProximo();
        return loteRepository.findVenciendo(hoy, hoy.plusDays(dias)).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Lotes con el semaforo en estado critico (ROJO).
     */
    @Transactional(readOnly = true)
    public List<LoteResponse> getCriticos() {
        LocalDate hoy = LocalDate.now();
        int dias = configuracionService.getDiasCritico();
        return loteRepository.findVenciendo(hoy, hoy.plusDays(dias)).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Sincroniza a VENCIDO el estado de los lotes cuya fecha de vencimiento ya paso (RN03).
     * Se ejecuta al abrir las pantallas de inventario y tras cada operacion.
     */
    @Transactional
    public int sincronizarVencidos() {
        LocalDate hoy = LocalDate.now();
        List<Lote> vencidos = loteRepository.findVencidosConStock(hoy);
        for (Lote lote : vencidos) {
            lote.setEstado(Lote.Estado.VENCIDO);
            loteRepository.save(lote);
        }
        return vencidos.size();
    }
    
    private void registrarMovimiento(Lote lote, Usuario user, MovimientoInventario.TipoMovimiento tipo,
                                     Integer cantidad, Integer stockAnterior, Integer stockNuevo,
                                     String referenciaTipo, Long referenciaId) {
        MovimientoInventario movimiento = MovimientoInventario.builder()
                .medicamento(lote.getMedicamento())
                .lote(lote)
                .usuario(user)
                .tipoMovimiento(tipo)
                .cantidad(cantidad)
                .stockAnterior(stockAnterior)
                .stockNuevo(stockNuevo)
                .referenciaTipo(referenciaTipo)
                .referenciaId(referenciaId)
                .build();
        movimientoRepository.save(movimiento);
    }
    
    private LoteResponse mapToResponse(Lote l) {
        int diasCritico = configuracionService.getDiasCritico();
        int diasProximo = configuracionService.getDiasProximo();
        
        return LoteResponse.builder()
                .id(l.getId())
                .medicamentoId(l.getMedicamento().getId())
                .medicamentoCodigo(l.getMedicamento().getCodigo())
                .medicamentoNombre(l.getMedicamento().getNombre())
                .unidadMedida(l.getMedicamento().getUnidadMedida())
                .proveedorRazonSocial(l.getMedicamento().getProveedor() != null
                        ? l.getMedicamento().getProveedor().getRazonSocial() : null)
                .numeroLote(l.getNumeroLote())
                .fechaIngreso(l.getFechaIngreso())
                .fechaVencimiento(l.getFechaVencimiento())
                .cantidadInicial(l.getCantidadInicial())
                .cantidadActual(l.getCantidadActual())
                .estado(l.getEstado())
                .estadoVencimiento(l.getEstadoVencimiento(diasCritico, diasProximo))
                .diasRestantes(l.getDiasRestantes())
                .createdBy(l.getCreatedBy())
                .createdAt(l.getCreatedAt())
                .updatedAt(l.getUpdatedAt())
                .build();
    }
}
