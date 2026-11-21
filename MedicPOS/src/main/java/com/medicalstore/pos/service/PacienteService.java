package com.medicalstore.pos.service;

import com.medicalstore.pos.dto.request.CreatePacienteRequest;
import com.medicalstore.pos.dto.response.PacienteResponse;
import com.medicalstore.pos.entity.Paciente;
import com.medicalstore.pos.entity.Usuario;
import com.medicalstore.pos.repository.PacienteRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Soporte de pacientes para la dispensacion. RN05.0: el paciente se identifica por DNI.
 */
@Service
public class PacienteService {
    
    private final PacienteRepository pacienteRepository;
    
    public PacienteService(PacienteRepository pacienteRepository) {
        this.pacienteRepository = pacienteRepository;
    }
    
    @Transactional(readOnly = true)
    public List<PacienteResponse> getAll() {
        return pacienteRepository.findByActivoTrueOrderByNombresAsc().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<PacienteResponse> buscar(String texto) {
        if (texto == null || texto.trim().length() < 2) {
            return List.of();
        }
        return pacienteRepository.buscar(texto.trim()).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public PacienteResponse getById(Long id) {
        return mapToResponse(obtenerEntidad(id));
    }
    
    @Transactional(readOnly = true)
    public Paciente obtenerEntidad(Long id) {
        return pacienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Paciente not found with id: " + id));
    }
    
    @Transactional
    public PacienteResponse create(CreatePacienteRequest request, Usuario user, HttpServletRequest httpRequest) {
        if (pacienteRepository.existsByDni(request.getDni())) {
            throw new RuntimeException("El DNI " + request.getDni() + " ya esta registrado");
        }
        
        Paciente paciente = Paciente.builder()
                .dni(request.getDni())
                .nombres(request.getNombres())
                .apellidos(request.getApellidos())
                .activo(true)
                .createdBy(user != null ? user.getId() : null)
                .build();
        
        paciente = pacienteRepository.save(paciente);
        
        return mapToResponse(paciente);
    }
    
    private PacienteResponse mapToResponse(Paciente p) {
        return PacienteResponse.builder()
                .id(p.getId())
                .dni(p.getDni())
                .nombres(p.getNombres())
                .apellidos(p.getApellidos())
                .codigoExterno(p.getCodigoExterno())
                .fullName(p.getNombreCompleto())
                .activo(p.getActivo())
                .createdAt(p.getCreatedAt())
                .build();
    }
}
