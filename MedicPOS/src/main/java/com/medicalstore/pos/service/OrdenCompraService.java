package com.medicalstore.pos.service;

import com.medicalstore.pos.dto.request.CreateOrdenCompraRequest;
import com.medicalstore.pos.dto.request.OrdenCompraItemRequest;
import com.medicalstore.pos.dto.request.ResolverOrdenCompraRequest;
import com.medicalstore.pos.dto.response.OrdenCompraDetalleResponse;
import com.medicalstore.pos.dto.response.OrdenCompraResponse;
import com.medicalstore.pos.dto.response.SugerenciaCompraResponse;
import com.medicalstore.pos.entity.Auditoria;
import com.medicalstore.pos.entity.DetalleOrdenCompra;
import com.medicalstore.pos.entity.Medicamento;
import com.medicalstore.pos.entity.OrdenCompra;
import com.medicalstore.pos.entity.Proveedor;
import com.medicalstore.pos.entity.Usuario;
import com.medicalstore.pos.repository.LoteRepository;
import com.medicalstore.pos.repository.MedicamentoRepository;
import com.medicalstore.pos.repository.OrdenCompraRepository;
import com.medicalstore.pos.repository.ProveedorRepository;
import com.medicalstore.pos.repository.UsuarioRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * M06 Abastecimiento - ordenes de compra (RF-09, RF-10A, RN06, RN07).
 *
 * Logistica crea la orden y el Jefe de Farmacia la aprueba o rechaza.
 * Al aprobar queda habilitada la recepcion que ingestara los lotes (M03).
 */
@Service
public class OrdenCompraService {
    
    private final OrdenCompraRepository ordenRepository;
    private final ProveedorRepository proveedorRepository;
    private final MedicamentoRepository medicamentoRepository;
    private final LoteRepository loteRepository;
    private final UsuarioRepository usuarioRepository;
    private final AuditoriaService auditoriaService;
    
    public OrdenCompraService(OrdenCompraRepository ordenRepository,
                              ProveedorRepository proveedorRepository,
                              MedicamentoRepository medicamentoRepository,
                              LoteRepository loteRepository,
                              UsuarioRepository usuarioRepository,
                              AuditoriaService auditoriaService) {
        this.ordenRepository = ordenRepository;
        this.proveedorRepository = proveedorRepository;
        this.medicamentoRepository = medicamentoRepository;
        this.loteRepository = loteRepository;
        this.usuarioRepository = usuarioRepository;
        this.auditoriaService = auditoriaService;
    }
    
    @Transactional(rollbackFor = Exception.class)
    public OrdenCompraResponse create(CreateOrdenCompraRequest request, Usuario user, HttpServletRequest httpRequest) {
        Proveedor proveedor = proveedorRepository.findById(request.getProveedorId())
                .orElseThrow(() -> new RuntimeException("Proveedor not found with id: " + request.getProveedorId()));
        
        if (!proveedor.getActivo()) {
            throw new RuntimeException("No se puede crear una orden para el proveedor inactivo " + proveedor.getRazonSocial());
        }
        
        OrdenCompra orden = OrdenCompra.builder()
                .proveedor(proveedor)
                .usuario(user)
                .estado(OrdenCompra.Estado.BORRADOR)
                .observacion(request.getObservacion())
                .createdBy(user != null ? user.getId() : null)
                .build();
        
        Map<Long, Integer> acumulado = new LinkedHashMap<>();
        Map<Long, Medicamento> porId = new LinkedHashMap<>();
        for (OrdenCompraItemRequest item : request.getItems()) {
           Medicamento medicamento = medicamentoRepository.findByIdWithProveedor(item.getMedicamentoId())
                    .orElseThrow(() -> new RuntimeException("Medicamento not found with id: " + item.getMedicamentoId()));
            
            if (!medicamento.getActivo()) {
                throw new RuntimeException("El medicamento " + medicamento.getNombre() + " esta inactivo");
            }
            // RF-10A: una orden se dirige a un unico proveedor
            if (!medicamento.getProveedor().getId().equals(proveedor.getId())) {
                throw new RuntimeException("El medicamento " + medicamento.getNombre() 
                        + " no pertenece al proveedor " + proveedor.getRazonSocial());
            }
            
            porId.put(medicamento.getId(), medicamento);
            acumulado.merge(medicamento.getId(), item.getCantidad(), Integer::sum);
        }
        
        for (Map.Entry<Long, Integer> entry : acumulado.entrySet()) {
            agregarDetalle(orden, porId.get(entry.getKey()), entry.getValue());
        }
        
        orden = ordenRepository.save(orden);
        
        auditoriaService.log(Auditoria.Accion.ORDEN_CREADA, user, "OrdenCompra", orden.getId().toString(),
                "Orden de compra creada para " + proveedor.getRazonSocial()
                        + " con " + orden.getDetalles().size() + " medicamentos",
                null, orden.toString(), httpRequest);
        
        return mapToResponse(ordenRepository.findByIdDetallado(orden.getId()).orElse(orden));
    }
    
    @Transactional(rollbackFor = Exception.class)
    public OrdenCompraResponse resolver(Long id, ResolverOrdenCompraRequest request, Usuario user, HttpServletRequest httpRequest) {
        OrdenCompra orden = ordenRepository.findByIdDetallado(id)
                .orElseThrow(() -> new RuntimeException("OrdenCompra not found with id: " + id));
        
        if (orden.getEstado() != OrdenCompra.Estado.BORRADOR && orden.getEstado() != OrdenCompra.Estado.SUGERIDA) {
            throw new RuntimeException("La orden ya fue resuelta y no puede volver a cambiar de estado");
        }
        if (request.getDecision() != OrdenCompra.Estado.APROBADA
                && request.getDecision() != OrdenCompra.Estado.RECHAZADA) {
            throw new RuntimeException("La decision debe ser APROBADA o RECHAZADA");
        }
        
        String estadoAnterior = orden.getEstado().toString();
        orden.setEstado(request.getDecision());
        if (request.getObservacion() != null && !request.getObservacion().isBlank()) {
            orden.setObservacion(request.getObservacion());
        }
        
        if (request.getDecision() == OrdenCompra.Estado.APROBADA) {
            orden.setFechaAprobacion(java.time.LocalDateTime.now());
            orden.setAprobadoPor(user.getId());
        }
        orden.setUpdatedBy(user != null ? user.getId() : null);
        orden = ordenRepository.save(orden);
        
        auditoriaService.log(
                request.getDecision() == OrdenCompra.Estado.APROBADA
                        ? Auditoria.Accion.ORDEN_APROBADA : Auditoria.Accion.ORDEN_RECHAZADA,
                user, "OrdenCompra", id.toString(),
                "Orden " + id + " " + request.getDecision().toString().toLowerCase(),
                estadoAnterior, request.getDecision().toString(), httpRequest);
        
        return mapToResponse(ordenRepository.findByIdDetallado(id).orElse(orden));
    }
    
    private void agregarDetalle(OrdenCompra orden, Medicamento medicamento, int cantidad) {
        orden.agregarDetalle(DetalleOrdenCompra.builder()
                .medicamento(medicamento)
                .cantidad(cantidad)
                .sugerido(false)
                .build());
    }
    
    /**
     * RF-09: pedido sugerido por el sistema a partir de los medicamentos en stock critico.
     * Solo es una propuesta: el Jefe de Farmacia decide si se convierte en orden.
     */
    @Transactional(readOnly = true)
    public List<SugerenciaCompraResponse> getSugerencias() {
        LocalDate hoy = LocalDate.now();
        List<SugerenciaCompraResponse> sugerencias = new ArrayList<>();
        
        for (Medicamento medicamento : medicamentoRepository.findAllActivosWithProveedor()) {
            int stockVigente = loteRepository.getStockVigente(medicamento.getId(), hoy);
            
            // RN06: solo entra en la sugerencia el stock que igualo o bajo su minimo
            if (!medicamento.isStockCritico(stockVigente)) {
                continue;
            }
            int cantidadSugerida = medicamento.getCantidadSugerida(stockVigente);
            if (cantidadSugerida == 0) {
                continue;
            }
            
            sugerencias.add(SugerenciaCompraResponse.builder()
                    .medicamentoId(medicamento.getId())
                    .codigo(medicamento.getCodigo())
                    .nombre(medicamento.getNombre())
                    .unidadMedida(medicamento.getUnidadMedida())
                    .stockActual(stockVigente)
                    .stockMinimo(medicamento.getStockMinimo())
                    .stockMaximo(medicamento.getStockMaximo())
                    .cantidadSugerida(cantidadSugerida)
                    .proveedorId(medicamento.getProveedor().getId())
                    .proveedorRazonSocial(medicamento.getProveedor().getRazonSocial())
                    .build());
        }
        
        return sugerencias.stream()
                .sorted((a, b) -> b.getCantidadSugerida().compareTo(a.getCantidadSugerida()))
                .collect(Collectors.toList());
    }
    
    /**
     * Convierte la propuesta automatica en una orden en estado SUGERIDA para su aprobacion.
     */
    @Transactional(rollbackFor = Exception.class)
    public OrdenCompraResponse crearDesdeSugerencia(List<SugerenciaCompraResponse> sugerencias, Usuario user,
                                                    String observacion, HttpServletRequest httpRequest) {
        if (sugerencias == null || sugerencias.isEmpty()) {
            throw new RuntimeException("La sugerencia no tiene medicamentos para ordenar");
        }
        
        Map<Long, List<SugerenciaCompraResponse>> porProveedor = new LinkedHashMap<>();
        for (SugerenciaCompraResponse s : sugerencias) {
            porProveedor.computeIfAbsent(s.getProveedorId(), k -> new ArrayList<>()).add(s);
        }
        
        if (porProveedor.size() > 1) {
            throw new RuntimeException("Una orden de compra solo puede abarcar un proveedor, "
                    + "se recibieron " + porProveedor.size());
        }
        
        Map.Entry<Long, List<SugerenciaCompraResponse>> unico = porProveedor.entrySet().iterator().next();
        Proveedor proveedor = proveedorRepository.findById(unico.getKey())
                .orElseThrow(() -> new RuntimeException("Proveedor not found with id: " + unico.getKey()));
        
        OrdenCompra orden = OrdenCompra.builder()
                .proveedor(proveedor)
                .usuario(user)
                .estado(OrdenCompra.Estado.SUGERIDA)
                .observacion(observacion != null ? observacion : "Pedido sugerido por el sistema (RF-09)")
                .createdBy(user != null ? user.getId() : null)
                .build();
        
        for (SugerenciaCompraResponse s : unico.getValue()) {
            Medicamento medicamento = medicamentoRepository.findByIdWithProveedor(s.getMedicamentoId())
                    .orElseThrow(() -> new RuntimeException("Medicamento not found with id: " + s.getMedicamentoId()));
            orden.agregarDetalle(DetalleOrdenCompra.builder()
                    .medicamento(medicamento)
                    .cantidad(s.getCantidadSugerida())
                    .sugerido(true)
                    .build());
        }
        
        orden = ordenRepository.save(orden);
        
        auditoriaService.log(Auditoria.Accion.ORDEN_CREADA, user, "OrdenCompra", orden.getId().toString(),
                "Orden sugerida generada para " + proveedor.getRazonSocial()
                        + " con " + orden.getDetalles().size() + " medicamentos",
                null, orden.toString(), httpRequest);
        
        return mapToResponse(ordenRepository.findByIdDetallado(orden.getId()).orElse(orden));
    }
    
    @Transactional(readOnly = true)
    public List<OrdenCompraResponse> getAll(OrdenCompra.Estado estado, Long proveedorId) {
        List<OrdenCompra> ordenes;
        if (estado != null && proveedorId != null) {
            ordenes = ordenRepository.findByEstadoAndProveedorIdDetallado(estado, proveedorId);
        } else if (estado != null) {
            ordenes = ordenRepository.findByEstadoDetallado(estado);
        } else if (proveedorId != null) {
            ordenes = ordenRepository.findByProveedorIdDetallado(proveedorId);
        } else {
            ordenes = ordenRepository.findAllDetalladoOrderByFechaDesc();
        }
        return ordenes.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public OrdenCompraResponse getById(Long id) {
        return mapToResponse(ordenRepository.findByIdDetallado(id)
                .orElseThrow(() -> new RuntimeException("OrdenCompra not found with id: " + id)));
    }
    
    @Transactional(readOnly = true)
    public long countByEstado(OrdenCompra.Estado estado) {
        return ordenRepository.countByEstado(estado);
    }
    
    private OrdenCompraResponse mapToResponse(OrdenCompra o) {
        Proveedor proveedor = o.getProveedor();
        Usuario usuario = o.getUsuario();
        Usuario aprobador = o.getAprobadoPor() != null
                ? usuarioRepository.findById(o.getAprobadoPor()).orElse(null) : null;
        LocalDate hoy = LocalDate.now();
        
        List<OrdenCompraDetalleResponse> detalles = o.getDetalles().stream()
                .map(d -> OrdenCompraDetalleResponse.builder()
                        .id(d.getId())
                        .medicamentoId(d.getMedicamento().getId())
                        .medicamentoCodigo(d.getMedicamento().getCodigo())
                        .medicamentoNombre(d.getMedicamento().getNombre())
                        .unidadMedida(d.getMedicamento().getUnidadMedida())
                        .cantidad(d.getCantidad())
                        .stockActual(loteRepository.getStockVigente(d.getMedicamento().getId(), hoy))
                        .sugerido(d.getSugerido())
                        .build())
                .collect(Collectors.toList());
        
        int totalUnidades = detalles.stream()
                .mapToInt(d -> d.getCantidad() != null ? d.getCantidad() : 0)
                .sum();
        
        return OrdenCompraResponse.builder()
                .id(o.getId())
                .estado(o.getEstado())
                .observacion(o.getObservacion())
                .fechaHora(o.getFechaHora())
                .fechaAprobacion(o.getFechaAprobacion())
                .proveedorId(proveedor.getId())
                .proveedorRuc(proveedor.getRuc())
                .proveedorRazonSocial(proveedor.getRazonSocial())
                .usuarioId(usuario != null ? usuario.getId() : null)
                .usuarioNombre(usuario != null ? usuario.getNombreCompleto() : null)
                .usuarioRol(usuario != null ? usuario.getNombreRol() : null)
                .aprobadoPorId(o.getAprobadoPor())
                .aprobadoPorNombre(aprobador != null ? aprobador.getNombreCompleto() : null)
                .detalles(detalles)
                .totalItems(detalles.size())
                .totalUnidades(totalUnidades)
                .createdAt(o.getCreatedAt())
                .updatedAt(o.getUpdatedAt())
                .build();
    }
}
