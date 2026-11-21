package com.medicalstore.pos.service;

import com.medicalstore.pos.dto.request.CreateProveedorRequest;
import com.medicalstore.pos.dto.request.UpdateProveedorRequest;
import com.medicalstore.pos.dto.response.ProveedorResponse;
import com.medicalstore.pos.entity.Auditoria;
import com.medicalstore.pos.entity.Proveedor;
import com.medicalstore.pos.entity.Usuario;
import com.medicalstore.pos.repository.MedicamentoRepository;
import com.medicalstore.pos.repository.ProveedorRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * M06 Proveedores (RF-10, RN07). Logistica crea, modifica y desactiva.
 */
@Service
public class ProveedorService {
    
    private final ProveedorRepository proveedorRepository;
    private final MedicamentoRepository medicamentoRepository;
    private final AuditoriaService auditoriaService;
    
    public ProveedorService(ProveedorRepository proveedorRepository,
                            MedicamentoRepository medicamentoRepository,
                            AuditoriaService auditoriaService) {
        this.proveedorRepository = proveedorRepository;
        this.medicamentoRepository = medicamentoRepository;
        this.auditoriaService = auditoriaService;
    }
    
    @Transactional(readOnly = true)
    public List<ProveedorResponse> getAll(boolean soloActivos) {
        List<Proveedor> proveedores = soloActivos
                ? proveedorRepository.findByActivoTrueOrderByRazonSocialAsc()
                : proveedorRepository.findAll();
        return proveedores.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<ProveedorResponse> buscar(String texto) {
        if (texto == null || texto.trim().length() < 2) {
            return List.of();
        }
        return proveedorRepository.buscar(texto.trim()).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public ProveedorResponse getById(Long id) {
        return mapToResponse(obtenerEntidad(id));
    }
    
    @Transactional(readOnly = true)
    public Proveedor obtenerEntidad(Long id) {
        return proveedorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Proveedor not found with id: " + id));
    }
    
    @Transactional
    public ProveedorResponse create(CreateProveedorRequest request, Usuario user, HttpServletRequest httpRequest) {
        if (proveedorRepository.existsByRuc(request.getRuc())) {
            throw new RuntimeException("El RUC " + request.getRuc() + " ya esta registrado");
        }
        
        Proveedor proveedor = Proveedor.builder()
                .ruc(request.getRuc())
                .razonSocial(request.getRazonSocial())
                .contacto(request.getContacto())
                .telefono(request.getTelefono())
                .correo(request.getCorreo())
                .activo(true)
                .createdBy(user != null ? user.getId() : null)
                .build();
        
        proveedor = proveedorRepository.save(proveedor);
        
        auditoriaService.log(Auditoria.Accion.PROVEEDOR_CREADO, user, "Proveedor", proveedor.getId().toString(),
                "Proveedor creado: " + proveedor.getRazonSocial(), null, proveedor.toString(), httpRequest);
        
        return mapToResponse(proveedor);
    }
    
    @Transactional
    public ProveedorResponse update(Long id, UpdateProveedorRequest request, Usuario user, HttpServletRequest httpRequest) {
        Proveedor proveedor = obtenerEntidad(id);
        String valorAnterior = proveedor.toString();
        
        proveedor.setRazonSocial(request.getRazonSocial());
        proveedor.setContacto(request.getContacto());
        proveedor.setTelefono(request.getTelefono());
        proveedor.setCorreo(request.getCorreo());
        proveedor.setActivo(Boolean.parseBoolean(request.getActivo()));
        proveedor.setUpdatedBy(user != null ? user.getId() : null);
        
        if (!proveedor.getActivo()) {
            proveedor.setDeletedBy(user != null ? user.getId() : null);
            proveedor.setDeletedAt(LocalDateTime.now());
        } else {
            proveedor.setDeletedBy(null);
            proveedor.setDeletedAt(null);
        }
        
        proveedor = proveedorRepository.save(proveedor);
        
        auditoriaService.log(
                proveedor.getActivo() ? Auditoria.Accion.PROVEEDOR_ACTUALIZADO : Auditoria.Accion.PROVEEDOR_DESACTIVADO,
                user, "Proveedor", id.toString(),
                "Proveedor actualizado: " + proveedor.getRazonSocial(),
                valorAnterior, proveedor.toString(), httpRequest);
        
        return mapToResponse(proveedor);
    }
    
    /**
     * Un proveedor con medicamentos activos asociados no puede desactivarse.
     */
    @Transactional
    public ProveedorResponse toggleActivo(Long id, Usuario user, HttpServletRequest httpRequest) {
        Proveedor proveedor = obtenerEntidad(id);
        boolean nuevoEstado = !proveedor.getActivo();
        
        if (!nuevoEstado) {
            List<com.medicalstore.pos.entity.Medicamento> medicamentos = 
                    medicamentoRepository.findByProveedorIdAndActivoTrue(id);
            if (!medicamentos.isEmpty()) {
                throw new RuntimeException("No se puede desactivar un proveedor con " + medicamentos.size() 
                        + " medicamentos activos asociados");
            }
        }
        
        String valorAnterior = proveedor.getActivo() ? "activo" : "inactivo";
        proveedor.setActivo(nuevoEstado);
        proveedor.setUpdatedBy(user != null ? user.getId() : null);
        proveedor.setDeletedBy(nuevoEstado ? null : (user != null ? user.getId() : null));
        proveedor.setDeletedAt(nuevoEstado ? null : LocalDateTime.now());
        proveedor = proveedorRepository.save(proveedor);
        
        auditoriaService.log(
                nuevoEstado ? Auditoria.Accion.PROVEEDOR_ACTUALIZADO : Auditoria.Accion.PROVEEDOR_DESACTIVADO,
                user, "Proveedor", id.toString(),
                (nuevoEstado ? "Proveedor activado: " : "Proveedor desactivado: ") + proveedor.getRazonSocial(),
                valorAnterior, nuevoEstado ? "activo" : "inactivo", httpRequest);
        
        return mapToResponse(proveedor);
    }
    
    private ProveedorResponse mapToResponse(Proveedor p) {
        return ProveedorResponse.builder()
                .id(p.getId())
                .ruc(p.getRuc())
                .razonSocial(p.getRazonSocial())
                .contacto(p.getContacto())
                .telefono(p.getTelefono())
                .correo(p.getCorreo())
                .activo(p.getActivo())
                .medicamentosAsociados((long) medicamentoRepository.findByProveedorIdAndActivoTrue(p.getId()).size())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }
}
