package com.medicalstore.pos.service;

import com.medicalstore.pos.dto.request.CreateRecetaRequest;
import com.medicalstore.pos.dto.response.RecetaResponse;
import com.medicalstore.pos.entity.Auditoria;
import com.medicalstore.pos.entity.Dispensacion;
import com.medicalstore.pos.entity.Medico;
import com.medicalstore.pos.entity.Paciente;
import com.medicalstore.pos.entity.Receta;
import com.medicalstore.pos.entity.Usuario;
import com.medicalstore.pos.repository.DispensacionRepository;
import com.medicalstore.pos.repository.MedicoRepository;
import com.medicalstore.pos.repository.PacienteRepository;
import com.medicalstore.pos.repository.RecetaRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Year;
import java.util.List;
import java.util.stream.Collectors;

/**
 * M04 Dispensacion - recetas (RF-05A, RF-05B, RN05.0).
 * Sin numero de receta el sistema no deja avanzar.
 * El numero de receta es auto-generado con formato REC-YYYY-NNNNN si no se proporciona.
 */
@Service
public class RecetaService {
    
    private final RecetaRepository recetaRepository;
    private final PacienteRepository pacienteRepository;
    private final MedicoRepository medicoRepository;
    private final DispensacionRepository dispensacionRepository;
    private final AuditoriaService auditoriaService;
    
    public RecetaService(RecetaRepository recetaRepository,
                         PacienteRepository pacienteRepository,
                         MedicoRepository medicoRepository,
                         DispensacionRepository dispensacionRepository,
                         AuditoriaService auditoriaService) {
        this.recetaRepository = recetaRepository;
        this.pacienteRepository = pacienteRepository;
        this.medicoRepository = medicoRepository;
        this.dispensacionRepository = dispensacionRepository;
        this.auditoriaService = auditoriaService;
    }
    
    @Transactional(readOnly = true)
    public List<RecetaResponse> getAll(Receta.TipoReceta tipo, LocalDate desde, LocalDate hasta, Boolean pendiente) {
        List<Receta> recetas;
        boolean conRango = desde != null && hasta != null;
        if (Boolean.TRUE.equals(pendiente)) {
            recetas = recetaRepository.findPendientesWithPaciente();
        } else if (tipo != null && conRango) {
            recetas = recetaRepository.findByTipoAndRangoConPaciente(tipo, desde, hasta);
        } else if (tipo != null) {
            recetas = recetaRepository.findByTipoWithPaciente(tipo);
        } else if (conRango) {
            recetas = recetaRepository.findByRangoFechasConPaciente(desde, hasta);
        } else {
            recetas = recetaRepository.findAllWithPacienteOrderByFechaDesc();
        }
        return recetas.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public RecetaResponse getById(Long id) {
        return mapToResponse(obtenerEntidad(id));
    }
    
    @Transactional(readOnly = true)
    public Receta obtenerEntidad(Long id) {
        return recetaRepository.findByIdWithPaciente(id)
                .orElseThrow(() -> new RuntimeException("Receta not found with id: " + id));
    }
    
    /**
     * Genera el siguiente numero de receta con formato REC-YYYY-NNNNN.
     * Ejemplo: REC-2026-00001
     * Usa bloqueo pesimista para evitar duplicados en concurrencia.
     */
    @Transactional
    public String generarSiguienteNumeroReceta() {
        String patron = "REC-" + Year.now().getValue() + "-%";
        Integer maxSecuencia = recetaRepository.findMaxSecuenciaByAno(patron);
        int siguiente = (maxSecuencia != null ? maxSecuencia : 0) + 1;
        return String.format("REC-%d-%05d", Year.now().getValue(), siguiente);
    }
    
@Transactional
    public RecetaResponse create(CreateRecetaRequest request, Usuario user, HttpServletRequest httpRequest) {
        // Si no se proporciona numeroReceta, generar uno automaticamente
        String numeroReceta = request.getNumeroReceta();
        if (numeroReceta == null || numeroReceta.trim().isEmpty()) {
            numeroReceta = generarSiguienteNumeroReceta();
        } else {
            numeroReceta = numeroReceta.trim();
            if (recetaRepository.existsByNumeroReceta(numeroReceta)) {
                throw new RuntimeException("La receta " + numeroReceta + " ya esta registrada");
            }
        }

        Paciente paciente = pacienteRepository.findById(request.getPacienteId())
                .orElseThrow(() -> new RuntimeException("Paciente not found with id: " + request.getPacienteId()));

        // Validar que el medico existe
        if (request.getMedicoId() == null) {
            throw new RuntimeException("El medico es obligatorio");
        }

        Medico medico = medicoRepository.findById(request.getMedicoId())
                .orElseThrow(() -> new RuntimeException("Medico not found with id: " + request.getMedicoId()));

        String numeroRecetaFinal = numeroReceta;

        Receta receta = Receta.builder()
                .paciente(paciente)
                .numeroReceta(numeroRecetaFinal)
                .medico(medico.getNombre())
                .tipoReceta(request.getTipoReceta())
                .fechaReceta(request.getFechaReceta())
                .createdBy(user != null ? user.getId() : null)
                .build();

        receta = recetaRepository.save(receta);

        auditoriaService.log(Auditoria.Accion.RECETA_REGISTRADA, user, "Receta", receta.getId().toString(),
                "Receta registrada: " + receta.getNumeroReceta() + " (" + receta.getTipoReceta() + ")",
                null, receta.toString(), httpRequest);

        return mapToResponse(receta);
    }
    
    private RecetaResponse mapToResponse(Receta r) {
        Dispensacion dispensacion = dispensacionRepository.findByRecetaId(r.getId()).orElse(null);
        Paciente paciente = r.getPaciente();
        
        return RecetaResponse.builder()
                .id(r.getId())
                .numeroReceta(r.getNumeroReceta())
                .medico(r.getMedico())
                .tipoReceta(r.getTipoReceta())
                .fechaReceta(r.getFechaReceta())
                .codigoExterno(r.getCodigoExterno())
                .pacienteId(paciente != null ? paciente.getId() : null)
                .pacienteDni(paciente != null ? paciente.getDni() : null)
                .pacienteNombres(paciente != null ? paciente.getNombres() : null)
                .pacienteApellidos(paciente != null ? paciente.getApellidos() : null)
                .pacienteNombreCompleto(paciente != null ? paciente.getNombreCompleto() : null)
                .tieneDispensacion(dispensacion != null)
                .dispensacionId(dispensacion != null ? dispensacion.getId() : null)
                .createdAt(r.getCreatedAt())
                .build();
    }
}
