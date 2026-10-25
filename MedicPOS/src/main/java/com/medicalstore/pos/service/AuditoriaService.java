package com.medicalstore.pos.service;

import com.medicalstore.pos.dto.response.AuditoriaResponse;
import com.medicalstore.pos.entity.Auditoria;
import com.medicalstore.pos.entity.Usuario;
import com.medicalstore.pos.repository.AuditoriaRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * RF-15: registro de auditoria de operaciones (RN08).
 * Se persiste en una transaccion independiente para no perder la traza si la operacion falla.
 */
@Service
public class AuditoriaService {
    
    private final AuditoriaRepository auditoriaRepository;
    
    public AuditoriaService(AuditoriaRepository auditoriaRepository) {
        this.auditoriaRepository = auditoriaRepository;
    }
    
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(Auditoria.Accion accion, Usuario user, String entidad, String referenciaId,
                    String descripcion, String valorAnterior, String valorNuevo, HttpServletRequest request) {
        if (user == null) {
            return;
        }
        
        Auditoria auditoria = Auditoria.builder()
                .usuario(user)
                .entidad(entidad)
                .accion(accion.name())
                .descripcion(descripcion)
                .valorAnterior(valorAnterior)
                .valorNuevo(valorNuevo)
                .fechaHora(LocalDateTime.now())
                .build();
        
        auditoriaRepository.save(auditoria);
    }
    
    @Transactional(readOnly = true)
    public List<AuditoriaResponse> getAll() {
        return auditoriaRepository.findAllWithUsuario().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<AuditoriaResponse> getByEntidad(String entidad) {
        return auditoriaRepository.findByEntidadOrderByFechaHoraDesc(entidad).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<AuditoriaResponse> getByUsuario(Long usuarioId) {
        return auditoriaRepository.findByUsuarioIdOrderByFechaHoraDesc(usuarioId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<AuditoriaResponse> getByRangoFechas(LocalDateTime desde, LocalDateTime hasta) {
        return auditoriaRepository.findByRangoFechas(desde, hasta).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * RN05.3: una alerta se considera atendida si existe un registro de auditoria
     * con accion ALERTA_ATENDIDA para su clave. No existe tabla de alertas.
     */
    @Transactional(readOnly = true)
    public List<Auditoria> getAlertasAtendidas() {
        return auditoriaRepository.findByEntidadAndAccion("Alerta", Auditoria.Accion.ALERTA_ATENDIDA.name());
    }
    
    private AuditoriaResponse mapToResponse(Auditoria a) {
        return AuditoriaResponse.builder()
                .id(a.getId())
                .usuarioId(a.getUsuario() != null ? a.getUsuario().getId() : null)
                .usuarioNombre(a.getUsuario() != null ? a.getUsuario().getNombreCompleto() : null)
                .usuarioRol(a.getUsuario() != null ? a.getUsuario().getNombreRol() : null)
                .entidad(a.getEntidad())
                .accion(a.getAccion())
                .descripcion(a.getDescripcion())
                .valorAnterior(a.getValorAnterior())
                .valorNuevo(a.getValorNuevo())
                .fechaHora(a.getFechaHora())
                .build();
    }
}
