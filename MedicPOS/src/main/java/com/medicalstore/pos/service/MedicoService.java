package com.medicalstore.pos.service;

import com.medicalstore.pos.dto.request.CreateMedicoRequest;
import com.medicalstore.pos.dto.request.UpdateMedicoRequest;
import com.medicalstore.pos.dto.response.MedicoResponse;
import com.medicalstore.pos.entity.Auditoria;
import com.medicalstore.pos.entity.Medico;
import com.medicalstore.pos.entity.Usuario;
import com.medicalstore.pos.repository.MedicoRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * M12 Medicos: gestion de medicos para recetas.
 */
@Service
public class MedicoService {

    private final MedicoRepository medicoRepository;
    private final AuditoriaService auditoriaService;

    public MedicoService(MedicoRepository medicoRepository,
                         AuditoriaService auditoriaService) {
        this.medicoRepository = medicoRepository;
        this.auditoriaService = auditoriaService;
    }

    @Transactional(readOnly = true)
    public List<MedicoResponse> getAllActivos() {
        return medicoRepository.findAllActivos().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MedicoResponse> buscar(String texto) {
        if (texto == null || texto.trim().length() < 2) {
            return List.of();
        }
        return medicoRepository.buscarActivos(texto.trim()).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public MedicoResponse getById(Long id) {
        return mapToResponse(obtenerEntidad(id));
    }

    @Transactional(readOnly = true)
    public Medico obtenerEntidad(Long id) {
        return medicoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Medico not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public List<MedicoResponse> getAll() {
        return medicoRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public MedicoResponse create(CreateMedicoRequest request, Usuario user, HttpServletRequest httpRequest) {
        if (medicoRepository.existsByNombre(request.getNombre())) {
            throw new RuntimeException("El medico " + request.getNombre() + " ya esta registrado");
        }

        Medico medico = Medico.builder()
                .nombre(request.getNombre())
                .especialidad(request.getEspecialidad())
                .activo(true)
                .createdBy(user != null ? user.getId() : null)
                .build();

        medico = medicoRepository.save(medico);

        auditoriaService.log(Auditoria.Accion.MEDICO_CREADO, user, "Medico",
                medico.getId().toString(), "Medico creado: " + medico.getNombre(),
                null, medico.toString(), httpRequest);

        return mapToResponse(medico);
    }

    @Transactional
    public MedicoResponse update(Long id, UpdateMedicoRequest request, Usuario user, HttpServletRequest httpRequest) {
        Medico medico = obtenerEntidad(id);

        if (!medico.getNombre().equals(request.getNombre()) && medicoRepository.existsByNombre(request.getNombre())) {
            throw new RuntimeException("El medico " + request.getNombre() + " ya esta registrado");
        }

        String valorAnterior = medico.toString();

        medico.setNombre(request.getNombre());
        medico.setEspecialidad(request.getEspecialidad());
        medico.setActivo(request.getActivo());
        medico.setUpdatedBy(user != null ? user.getId() : null);

        medico = medicoRepository.save(medico);

        auditoriaService.log(
                request.getActivo() ? Auditoria.Accion.MEDICO_ACTUALIZADO : Auditoria.Accion.MEDICO_DESACTIVADO,
                user, "Medico", id.toString(),
                "Medico actualizado: " + medico.getNombre(),
                valorAnterior, medico.toString(), httpRequest);

        return mapToResponse(medico);
    }

    @Transactional
    public MedicoResponse toggleActivo(Long id, Usuario user, HttpServletRequest httpRequest) {
        Medico medico = obtenerEntidad(id);
        boolean nuevoEstado = !medico.getActivo();

        String valorAnterior = medico.getActivo() ? "activo" : "inactivo";

        medico.setActivo(nuevoEstado);
        medico.setUpdatedBy(user != null ? user.getId() : null);
        medico = medicoRepository.save(medico);

        auditoriaService.log(
                nuevoEstado ? Auditoria.Accion.MEDICO_ACTUALIZADO : Auditoria.Accion.MEDICO_DESACTIVADO,
                user, "Medico", id.toString(),
                (nuevoEstado ? "Medico activado: " : "Medico desactivado: ") + medico.getNombre(),
                valorAnterior, nuevoEstado ? "activo" : "inactivo", httpRequest);

        return mapToResponse(medico);
    }

    private MedicoResponse mapToResponse(Medico m) {
        return MedicoResponse.builder()
                .id(m.getId())
                .nombre(m.getNombre())
                .especialidad(m.getEspecialidad())
                .activo(m.getActivo())
                .createdAt(m.getCreatedAt())
                .updatedAt(m.getUpdatedAt())
                .build();
    }
}