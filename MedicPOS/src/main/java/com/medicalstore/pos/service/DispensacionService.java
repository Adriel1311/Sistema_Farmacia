package com.medicalstore.pos.service;

import com.medicalstore.pos.dto.request.CreateDispensacionRequest;
import com.medicalstore.pos.dto.request.DispensacionItemRequest;
import com.medicalstore.pos.dto.response.DispensacionDetalleResponse;
import com.medicalstore.pos.dto.response.DispensacionResponse;
import com.medicalstore.pos.entity.Auditoria;
import com.medicalstore.pos.entity.DetalleDispensacion;
import com.medicalstore.pos.entity.Dispensacion;
import com.medicalstore.pos.entity.Lote;
import com.medicalstore.pos.entity.Lote.EstadoVencimiento;
import com.medicalstore.pos.entity.Medicamento;
import com.medicalstore.pos.entity.MovimientoInventario;
import com.medicalstore.pos.entity.Receta;
import com.medicalstore.pos.entity.Usuario;
import com.medicalstore.pos.repository.DispensacionRepository;
import com.medicalstore.pos.repository.LoteRepository;
import com.medicalstore.pos.repository.MedicamentoRepository;
import com.medicalstore.pos.repository.MovimientoInventarioRepository;
import com.medicalstore.pos.repository.RecetaRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * M04 Dispensacion (RF-06, RF-07, RN02, RN03, RN05.0, RN09).
 *
 * Reglas aplicadas:
 * - RN05.0: sin receta no hay atencion y una receta origina una sola dispensacion.
 * - RN02: FEFO. El sistema elige siempre el lote vigente que vence mas proximo.
 * - RF-04: solo el Jefe de Farmacia puede forzar otro lote, con motivo registrado y auditado.
 * - RN03: los lotes vencidos se bloquean aunque siga habiendo existencias fisicas.
 * - RN09: transaccion ACID con bloqueo pesimista para que el stock nunca quede negativo.
 */
@Service
public class DispensacionService {
    
    private static final String ROL_JEFE_FARMACIA = "JEFE_FARMACIA";
    
    private final DispensacionRepository dispensacionRepository;
    private final RecetaRepository recetaRepository;
    private final MedicamentoRepository medicamentoRepository;
    private final LoteRepository loteRepository;
    private final MovimientoInventarioRepository movimientoRepository;
    private final AuditoriaService auditoriaService;
    private final ConfiguracionService configuracionService;
    
    public DispensacionService(DispensacionRepository dispensacionRepository,
                               RecetaRepository recetaRepository,
                               MedicamentoRepository medicamentoRepository,
                               LoteRepository loteRepository,
                               MovimientoInventarioRepository movimientoRepository,
                               AuditoriaService auditoriaService,
                               ConfiguracionService configuracionService) {
        this.dispensacionRepository = dispensacionRepository;
        this.recetaRepository = recetaRepository;
        this.medicamentoRepository = medicamentoRepository;
        this.loteRepository = loteRepository;
        this.movimientoRepository = movimientoRepository;
        this.auditoriaService = auditoriaService;
        this.configuracionService = configuracionService;
    }
    
    /**
     * Registra la entrega de medicamentos asociada a una receta medica (RF-06).
     */
    @Transactional(isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public DispensacionResponse create(CreateDispensacionRequest request, Usuario user, HttpServletRequest httpRequest) {
        // RN05.0: la receta es obligatoria y no puede repetirse
        Receta receta = recetaRepository.findByIdWithPaciente(request.getRecetaId())
                .orElseThrow(() -> new RuntimeException("Receta not found with id: " + request.getRecetaId()));
        
        if (dispensacionRepository.existsByRecetaId(receta.getId())) {
            throw new RuntimeException("La receta " + receta.getNumeroReceta() + " ya fue dispensada");
        }
        
        // RN05.0: no se permiten dispensaciones vacias
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new RuntimeException("La dispensacion debe tener al menos un medicamento");
        }
        
        Dispensacion dispensacion = Dispensacion.builder()
                .receta(receta)
                .usuario(user)
                .estado(Dispensacion.Estado.COMPLETADA)
                .createdBy(user != null ? user.getId() : null)
                .build();
        
        LocalDate hoy = LocalDate.now();
        Map<String, String> excepcionesFefo = new LinkedHashMap<>();
        
        for (DispensacionItemRequest item : request.getItems()) {
            Medicamento medicamento = medicamentoRepository.findByIdWithProveedor(item.getMedicamentoId())
                    .orElseThrow(() -> new RuntimeException("Medicamento not found with id: " + item.getMedicamentoId()));
            
            if (!medicamento.getActivo()) {
                throw new RuntimeException("El medicamento " + medicamento.getNombre() + " esta inactivo");
            }
            
            if (item.getCantidad() == null || item.getCantidad() <= 0) {
                throw new RuntimeException("La cantidad de " + medicamento.getNombre() + " debe ser mayor a cero");
            }
            
            if (item.getLoteId() != null) {
                // RF-04: excepcion al FEFO, autorizada solo por el Jefe de Farmacia
                String motivo = dispensarLoteForzado(dispensacion, medicamento, item, user);
                excepcionesFefo.put(medicamento.getNombre(), motivo);
            } else {
                // RN02: FEFO automatico
                dispensarPorFefo(dispensacion, medicamento, item.getCantidad(), user, hoy);
            }
        }
        
        dispensacion = dispensacionRepository.save(dispensacion);
        
        auditoriaService.log(Auditoria.Accion.DISPENSACION_REGISTRADA, user, "Dispensacion", 
                dispensacion.getId().toString(),
                "Dispensacion de la receta " + receta.getNumeroReceta() + " (" + receta.getTipoReceta() + ")",
                null, dispensacion.toString(), httpRequest);
        
        // RF-04: toda excepcion FEFO queda auditada de forma independiente
        for (Map.Entry<String, String> excepcion : excepcionesFefo.entrySet()) {
            auditoriaService.log(Auditoria.Accion.EXCEPCION_FEFO, user, "Dispensacion", 
                    dispensacion.getId().toString(),
                    "Excepcion FEFO autorizada para " + excepcion.getKey(), null, excepcion.getValue(), httpRequest);
        }
        
        return mapToResponse(dispensacionRepository.findByIdDetallado(dispensacion.getId())
                .orElse(dispensacion));
    }
    
    /**
     * RN02: recorre los lotes vigentes en orden FEFO hasta cubrir la cantidad solicitada.
     * Si un lote esta vencido se omite y se genera la alerta de vencido bloqueado (RN03).
     */
    private void dispensarPorFefo(Dispensacion dispensacion, Medicamento medicamento, Integer cantidad,
                                  Usuario user, LocalDate hoy) {
        int pendiente = cantidad;
        List<Lote> lotes = loteRepository.findVigentesFEFOParaLockear(medicamento.getId(), hoy);
        
        if (lotes.isEmpty()) {
            throw new RuntimeException("No hay lotes vigentes de " + medicamento.getNombre() + " para dispensar");
        }
        
        for (Lote lote : lotes) {
            if (pendiente <= 0) {
                break;
            }
            
            // RN03: defensa en memoria, el filtro de la consulta ya lo cubre
            if (lote.isVencido() || lote.getEstado() == Lote.Estado.BAJA) {
                continue;
            }
            
            int aDescontar = Math.min(pendiente, lote.getCantidadActual());
            if (aDescontar <= 0) {
                continue;
            }
            
            int stockAnterior = lote.getCantidadActual();
            lote.setCantidadActual(stockAnterior - aDescontar);
            lote.setUpdatedBy(user != null ? user.getId() : null);
            loteRepository.save(lote);
            
            DetalleDispensacion detalle = DetalleDispensacion.builder()
                    .medicamento(medicamento)
                    .lote(lote)
                    .cantidad(aDescontar)
                    .build();
            dispensacion.agregarDetalle(detalle);
            
            registrarSalida(lote, user, aDescontar, stockAnterior, lote.getCantidadActual(), dispensacion);
            pendiente -= aDescontar;
        }
        
        if (pendiente > 0) {
            int disponible = lotes.stream().mapToInt(Lote::getCantidadActual).sum();
            throw new RuntimeException("Stock insuficiente de " + medicamento.getNombre() 
                    + ": se solicitan " + cantidad + " " + medicamento.getUnidadMedida() 
                    + " y solo hay " + disponible + " vigentes");
        }
    }
    
    /**
     * RF-04: el Jefe de Farmacia puede elegir un lote distinto al FEFO dejando el motivo.
     * Devuelve el motivo registrado para la trazabilidad.
     */
    private String dispensarLoteForzado(Dispensacion dispensacion, Medicamento medicamento,
                                        DispensacionItemRequest item, Usuario user) {
        if (user.getNombreRol() == null || !ROL_JEFE_FARMACIA.equals(user.getNombreRol())) {
            throw new RuntimeException("Solo el Jefe de Farmacia puede elegir un lote distinto al FEFO");
        }
        if (item.getMotivoExcepcionFefo() == null || item.getMotivoExcepcionFefo().trim().isEmpty()) {
            throw new RuntimeException("La excepcion FEFO requiere un motivo obligatorio");
        }
        
        LocalDate hoy = LocalDate.now();
        List<Lote> lotes = loteRepository.findVigentesFEFOParaLockear(medicamento.getId(), hoy);
        Lote loteFefo = lotes.isEmpty() ? null : lotes.get(0);
        
        Lote loteElegido = loteRepository.findByIdParaActualizar(item.getLoteId())
                .orElseThrow(() -> new RuntimeException("Lote not found with id: " + item.getLoteId()));
        
        if (!loteElegido.getMedicamento().getId().equals(medicamento.getId())) {
            throw new RuntimeException("El lote " + loteElegido.getNumeroLote() + " no corresponde al medicamento " + medicamento.getNombre());
        }
        
        // RN03: un lote vencido nunca puede dispensarse
        if (loteElegido.isVencido() || loteElegido.getEstado() == Lote.Estado.BAJA) {
            throw new RuntimeException("El lote " + loteElegido.getNumeroLote() + " esta vencido y no puede dispensarse");
        }
        if (loteFefo != null && loteFefo.getId().equals(loteElegido.getId())) {
            throw new RuntimeException("El lote seleccionado ya es el que indica el FEFO, no requiere excepcion");
        }
        if (!loteElegido.tieneStock(item.getCantidad())) {
            throw new RuntimeException("El lote " + loteElegido.getNumeroLote() + " no tiene stock suficiente");
        }
        
        int stockAnterior = loteElegido.getCantidadActual();
        loteElegido.setCantidadActual(stockAnterior - item.getCantidad());
        loteElegido.setUpdatedBy(user.getId());
        loteRepository.save(loteElegido);
        
        DetalleDispensacion detalle = DetalleDispensacion.builder()
                .medicamento(medicamento)
                .lote(loteElegido)
                .cantidad(item.getCantidad())
                .motivoExcepcionFefo(item.getMotivoExcepcionFefo())
                .build();
        dispensacion.agregarDetalle(detalle);
        
        registrarSalida(loteElegido, user, item.getCantidad(), stockAnterior, loteElegido.getCantidadActual(), dispensacion);
        
        return item.getMotivoExcepcionFefo();
    }
    
    private void registrarSalida(Lote lote, Usuario user, int cantidad, int stockAnterior, int stockNuevo,
                                 Dispensacion dispensacion) {
        MovimientoInventario movimiento = MovimientoInventario.builder()
                .medicamento(lote.getMedicamento())
                .lote(lote)
                .usuario(user)
                .tipoMovimiento(MovimientoInventario.TipoMovimiento.SALIDA)
                .cantidad(cantidad)
                .stockAnterior(stockAnterior)
                .stockNuevo(stockNuevo)
                .referenciaTipo("DISPENSACION")
                .referenciaId(dispensacion.getId() != null ? dispensacion.getId() : 0L)
                .build();
        movimientoRepository.save(movimiento);
    }
    
    private boolean esExcepcion(DetalleDispensacion detalle) {
        return detalle.getMotivoExcepcionFefo() != null && !detalle.getMotivoExcepcionFefo().trim().isEmpty();
    }
    
    @Transactional(readOnly = true)
    public List<DispensacionResponse> getAll() {
        return dispensacionRepository.findAllDetallado().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public DispensacionResponse getById(Long id) {
        return mapToResponse(dispensacionRepository.findByIdDetallado(id)
                .orElseThrow(() -> new RuntimeException("Dispensacion not found with id: " + id)));
    }
    
    @Transactional(readOnly = true)
    public List<DispensacionResponse> getByReceta(Long recetaId) {
        Dispensacion dispensacion = dispensacionRepository.findByRecetaIdConReceta(recetaId)
                .orElseThrow(() -> new RuntimeException("Dispensacion not found with receta id: " + recetaId));
        return List.of(mapToResponse(dispensacionRepository.findByIdDetallado(dispensacion.getId())
                .orElse(dispensacion)));
    }
    
    /**
     * Anula una dispensacion y devuelve el stock a los lotes de origen.
     */
    @Transactional(rollbackFor = Exception.class)
    public DispensacionResponse anular(Long id, Usuario user, HttpServletRequest httpRequest) {
        Dispensacion dispensacion = dispensacionRepository.findByIdDetallado(id)
                .orElseThrow(() -> new RuntimeException("Dispensacion not found with id: " + id));
        
        if (dispensacion.getEstado() == Dispensacion.Estado.ANULADA) {
            throw new RuntimeException("La dispensacion ya se encuentra anulada");
        }
        
        LocalDate hoy = LocalDate.now();
        for (DetalleDispensacion detalle : dispensacion.getDetalles()) {
            Lote lote = loteRepository.findByIdParaActualizar(detalle.getLote().getId())
                    .orElseThrow(() -> new RuntimeException("Lote not found with id: " + detalle.getLote().getId()));
            
            int stockAnterior = lote.getCantidadActual();
            lote.setCantidadActual(Math.min(lote.getCantidadInicial(), stockAnterior + detalle.getCantidad()));
            lote.setUpdatedBy(user != null ? user.getId() : null);
            loteRepository.save(lote);
            
            MovimientoInventario movimiento = MovimientoInventario.builder()
                    .medicamento(lote.getMedicamento())
                    .lote(lote)
                    .usuario(user)
                    .tipoMovimiento(MovimientoInventario.TipoMovimiento.AJUSTE)
                    .cantidad(detalle.getCantidad())
                    .stockAnterior(stockAnterior)
                    .stockNuevo(lote.getCantidadActual())
                    .referenciaTipo("ANULACION_DISPENSACION")
                    .referenciaId(dispensacion.getId())
                    .build();
            movimientoRepository.save(movimiento);
        }
        
        String valorAnterior = dispensacion.getEstado().toString();
        dispensacion.setEstado(Dispensacion.Estado.ANULADA);
        dispensacion = dispensacionRepository.save(dispensacion);
        
        auditoriaService.log(Auditoria.Accion.DISPENSACION_ANULADA, user, "Dispensacion", id.toString(),
                "Dispensacion anulada: receta " + dispensacion.getReceta().getNumeroReceta(),
                valorAnterior, Dispensacion.Estado.ANULADA.toString(), httpRequest);
        
        return mapToResponse(dispensacionRepository.findByIdDetallado(id).orElse(dispensacion));
    }
    
    private DispensacionResponse mapToResponse(Dispensacion d) {
        Receta receta = d.getReceta();
        List<DispensacionDetalleResponse> detalles = d.getDetalles().stream()
                .map(this::mapDetalle)
                .collect(Collectors.toList());
        
        int totalUnidades = detalles.stream()
                .mapToInt(det -> det.getCantidad() != null ? det.getCantidad() : 0)
                .sum();
        
        return DispensacionResponse.builder()
                .id(d.getId())
                .numeroTicket(d.getNumeroTicket())
                .estado(d.getEstado())
                .fechaHora(d.getFechaHora() != null ? d.getFechaHora() : LocalDateTime.now())
                .recetaId(receta.getId())
                .numeroReceta(receta.getNumeroReceta())
                .medico(receta.getMedico())
                .tipoReceta(receta.getTipoReceta().name())
                .fechaReceta(receta.getFechaReceta())
                .pacienteDni(receta.getPaciente() != null ? receta.getPaciente().getDni() : null)
                .pacienteNombreCompleto(receta.getPaciente() != null ? receta.getPaciente().getNombreCompleto() : null)
                .usuarioId(d.getUsuario() != null ? d.getUsuario().getId() : null)
                .usuarioNombre(d.getUsuario() != null ? d.getUsuario().getNombreCompleto() : null)
                .usuarioRol(d.getUsuario() != null ? d.getUsuario().getNombreRol() : null)
                .detalles(detalles)
                .totalItems(detalles.size())
                .totalUnidades(totalUnidades)
                .createdAt(d.getCreatedAt())
                .build();
    }
    
    private DispensacionDetalleResponse mapDetalle(DetalleDispensacion det) {
        Lote lote = det.getLote();
        // RN04: el semaforo usa los umbrales configurados, no valores fijos
        EstadoVencimiento estadoVencimiento = lote.getEstadoVencimiento(
                configuracionService.getDiasCritico(), configuracionService.getDiasProximo());
        return DispensacionDetalleResponse.builder()
                .id(det.getId())
                .medicamentoId(det.getMedicamento().getId())
                .medicamentoCodigo(det.getMedicamento().getCodigo())
                .medicamentoNombre(det.getMedicamento().getNombre())
                .unidadMedida(det.getMedicamento().getUnidadMedida())
                .loteId(lote.getId())
                .numeroLote(lote.getNumeroLote())
                .fechaVencimiento(lote.getFechaVencimiento())
                .estadoVencimiento(estadoVencimiento.name())
                .cantidad(det.getCantidad())
                .excepcionFefo(esExcepcion(det))
                .motivoExcepcionFefo(det.getMotivoExcepcionFefo())
                .build();
    }
}
