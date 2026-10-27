package com.medicalstore.pos.service;

import com.medicalstore.pos.dto.request.CreateMedicamentoRequest;
import com.medicalstore.pos.dto.request.UpdateMedicamentoRequest;
import com.medicalstore.pos.dto.response.MedicamentoResponse;
import com.medicalstore.pos.entity.Auditoria;
import com.medicalstore.pos.entity.Medicamento;
import com.medicalstore.pos.entity.Proveedor;
import com.medicalstore.pos.entity.Usuario;
import com.medicalstore.pos.exception.BusinessRuleException;
import com.medicalstore.pos.exception.ConflictException;
import com.medicalstore.pos.exception.ResourceNotFoundException;
import com.medicalstore.pos.repository.MedicamentoRepository;
import com.medicalstore.pos.repository.ProveedorRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * M02 Medicamentos (RF-01, RN01, RN06, RN07).
 * El Jefe de Farmacia registra y modifica; el resto de roles consulta.
 */
@Service
public class MedicamentoService {
    
    private final MedicamentoRepository medicamentoRepository;
    private final ProveedorRepository proveedorRepository;
    private final StockService stockService;
    private final AuditoriaService auditoriaService;
    
    public MedicamentoService(MedicamentoRepository medicamentoRepository,
                              ProveedorRepository proveedorRepository,
                              StockService stockService,
                              AuditoriaService auditoriaService) {
        this.medicamentoRepository = medicamentoRepository;
        this.proveedorRepository = proveedorRepository;
        this.stockService = stockService;
        this.auditoriaService = auditoriaService;
    }
    
    @Transactional(readOnly = true)
    public List<MedicamentoResponse> getAll(boolean soloActivos) {
        List<Medicamento> medicamentos = soloActivos
                ? medicamentoRepository.findAllActivosWithProveedor()
                : medicamentoRepository.findAll();
        return mapToResponseList(medicamentos);
    }
    
    @Transactional(readOnly = true)
    public List<MedicamentoResponse> buscar(String texto) {
        if (texto == null || texto.trim().length() < 2) {
            return List.of();
        }
        return mapToResponseList(medicamentoRepository.buscarActivos(texto.trim()));
    }
    
    @Transactional(readOnly = true)
    public MedicamentoResponse getById(Long id) {
        return mapToResponse(obtenerEntidad(id));
    }
    
    @Transactional(readOnly = true)
    public List<MedicamentoResponse> getByProveedor(Long proveedorId) {
        return mapToResponseList(medicamentoRepository.findByProveedorIdAndActivoTrue(proveedorId));
    }
    
    @Transactional(readOnly = true)
    public Medicamento obtenerEntidad(Long id) {
        return medicamentoRepository.findByIdWithProveedor(id)
                .orElseThrow(() -> new ResourceNotFoundException("Medicamento not found with id: " + id));
    }
    
    @Transactional
    public MedicamentoResponse create(CreateMedicamentoRequest request, Usuario user, HttpServletRequest httpRequest) {
        if (medicamentoRepository.existsByCodigo(request.getCodigo())) {
            throw new ConflictException("El codigo " + request.getCodigo() + " ya esta registrado");
        }
        validarRangos(request.getStockMinimo(), request.getStockMaximo());
        
        Proveedor proveedor = proveedorRepository.findById(request.getProveedorId())
                .orElseThrow(() -> new ResourceNotFoundException("Proveedor not found with id: " + request.getProveedorId()));
        if (!proveedor.getActivo()) {
            throw new BusinessRuleException("PROVEEDOR_INACTIVO", "No se puede asignar un proveedor inactivo: " + proveedor.getRazonSocial());
        }
        
        Medicamento medicamento = Medicamento.builder()
                .codigo(request.getCodigo())
                .nombre(request.getNombre())
                .descripcion(request.getDescripcion())
                .unidadMedida(request.getUnidadMedida())
                .proveedor(proveedor)
                .stockMinimo(request.getStockMinimo())
                .stockMaximo(request.getStockMaximo())
                .costoUnitario(costoUnitario(request.getCostoUnitario()))
                .activo(true)
                .createdBy(user != null ? user.getId() : null)
                .build();
        
        medicamento = medicamentoRepository.save(medicamento);
        
        auditoriaService.log(Auditoria.Accion.MEDICAMENTO_CREADO, user, "Medicamento", 
                medicamento.getId().toString(), "Medicamento creado: " + medicamento.getNombre(),
                null, medicamento.toString(), httpRequest);
        
        return mapToResponse(medicamento);
    }
    
    @Transactional
    public MedicamentoResponse update(Long id, UpdateMedicamentoRequest request, Usuario user, HttpServletRequest httpRequest) {
        Medicamento medicamento = obtenerEntidad(id);
        
        if (!medicamento.getCodigo().equals(request.getCodigo()) && medicamentoRepository.existsByCodigo(request.getCodigo())) {
            throw new ConflictException("El codigo " + request.getCodigo() + " ya esta registrado");
        }
        validarRangos(request.getStockMinimo(), request.getStockMaximo());
        
        Proveedor proveedor = proveedorRepository.findById(request.getProveedorId())
                .orElseThrow(() -> new ResourceNotFoundException("Proveedor not found with id: " + request.getProveedorId()));
        
        String valorAnterior = medicamento.toString();
        boolean estabaActivo = medicamento.getActivo();
        
        medicamento.setCodigo(request.getCodigo());
        medicamento.setNombre(request.getNombre());
        medicamento.setDescripcion(request.getDescripcion());
        medicamento.setUnidadMedida(request.getUnidadMedida());
        medicamento.setProveedor(proveedor);
        medicamento.setStockMinimo(request.getStockMinimo());
        medicamento.setStockMaximo(request.getStockMaximo());
        medicamento.setCostoUnitario(costoUnitario(request.getCostoUnitario()));
        medicamento.setActivo(request.getActivo());
        medicamento.setUpdatedBy(user != null ? user.getId() : null);
        
        if (!request.getActivo()) {
            medicamento.setDeletedBy(user != null ? user.getId() : null);
            medicamento.setDeletedAt(java.time.LocalDateTime.now());
        } else if (!estabaActivo) {
            medicamento.setDeletedBy(null);
            medicamento.setDeletedAt(null);
        }
        
        medicamento = medicamentoRepository.save(medicamento);
        
        auditoriaService.log(
                request.getActivo() ? Auditoria.Accion.MEDICAMENTO_ACTUALIZADO : Auditoria.Accion.MEDICAMENTO_DESACTIVADO,
                user, "Medicamento", id.toString(),
                "Medicamento actualizado: " + medicamento.getNombre(),
                valorAnterior, medicamento.toString(), httpRequest);
        
        return mapToResponse(medicamento);
    }
    
    /**
     * RN06b: baja logica. Un medicamento con existencias vigentes no puede desactivarse.
     */
    @Transactional
    public MedicamentoResponse toggleActivo(Long id, Usuario user, HttpServletRequest httpRequest) {
        Medicamento medicamento = obtenerEntidad(id);
        boolean nuevoEstado = !medicamento.getActivo();
        
        if (!nuevoEstado) {
            int stockVigente = stockService.getStockVigente(id, LocalDate.now());
            if (stockVigente > 0) {
                throw new BusinessRuleException("MEDICAMENTO_CON_STOCK", "No se puede desactivar un medicamento con " + stockVigente 
                        + " unidades vigentes en inventario");
            }
        }
        
        String valorAnterior = medicamento.getActivo() ? "activo" : "inactivo";
        medicamento.setActivo(nuevoEstado);
        medicamento.setUpdatedBy(user != null ? user.getId() : null);
        medicamento.setDeletedBy(nuevoEstado ? null : (user != null ? user.getId() : null));
        medicamento.setDeletedAt(nuevoEstado ? null : java.time.LocalDateTime.now());
        medicamento = medicamentoRepository.save(medicamento);
        
        auditoriaService.log(
                nuevoEstado ? Auditoria.Accion.MEDICAMENTO_ACTUALIZADO : Auditoria.Accion.MEDICAMENTO_DESACTIVADO,
                user, "Medicamento", id.toString(),
                (nuevoEstado ? "Medicamento activado: " : "Medicamento desactivado: ") + medicamento.getNombre(),
                valorAnterior, nuevoEstado ? "activo" : "inactivo", httpRequest);
        
        return mapToResponse(medicamento);
    }
    
    private void validarRangos(Integer stockMinimo, Integer stockMaximo) {
        if (stockMaximo <= stockMinimo) {
            throw new BusinessRuleException("RANGO_INVALIDO", "El stock maximo debe ser mayor al stock minimo");
        }
    }
    
    /**
     * Si el costo no viene informado se guarda en cero, para que el dato nunca
     * sea nulo en la base.
     */
    private BigDecimal costoUnitario(BigDecimal costo) {
        return costo == null ? BigDecimal.ZERO : costo;
    }
    
    private List<MedicamentoResponse> mapToResponseList(List<Medicamento> medicamentos) {
        if (medicamentos.isEmpty()) {
            return List.of();
        }
        
        LocalDate hoy = LocalDate.now();
        List<Long> ids = medicamentos.stream().map(Medicamento::getId).collect(Collectors.toList());
        Map<Long, Integer> stockVigenteMap = stockService.getStockVigentePorIds(ids, hoy);
        Map<Long, Integer> stockVencidoMap = stockService.getStockVencidoPorIds(ids, hoy);
        
        return medicamentos.stream()
                .map(m -> mapToResponse(m,
                        stockVigenteMap.getOrDefault(m.getId(), 0),
                        stockVencidoMap.getOrDefault(m.getId(), 0)))
                .collect(Collectors.toList());
    }
    
    private MedicamentoResponse mapToResponse(Medicamento m) {
        LocalDate hoy = LocalDate.now();
        return mapToResponse(m, stockService.getStockVigente(m.getId(), hoy), stockService.getStockVencido(m.getId(), hoy));
    }
    
    private MedicamentoResponse mapToResponse(Medicamento m, int stockVigente, int stockVencido) {
        return MedicamentoResponse.builder()
                .id(m.getId())
                .codigo(m.getCodigo())
                .nombre(m.getNombre())
                .descripcion(m.getDescripcion())
                .unidadMedida(m.getUnidadMedida())
                .proveedorId(m.getProveedor() != null ? m.getProveedor().getId() : null)
                .proveedorRazonSocial(m.getProveedor() != null ? m.getProveedor().getRazonSocial() : null)
                .stockMinimo(m.getStockMinimo())
                .stockMaximo(m.getStockMaximo())
                .costoUnitario(m.getCostoUnitario())
                .activo(m.getActivo())
                .stockVigente(stockVigente)
                .stockVencido(stockVencido)
                .cantidadSugerida(m.getCantidadSugerida(stockVigente))
                .stockCritico(m.isStockCritico(stockVigente))
                .lotesActivos((int) stockService.countLotesActivos(m.getId()))
                .createdAt(m.getCreatedAt())
                .updatedAt(m.getUpdatedAt())
                .build();
    }
}
