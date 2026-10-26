package com.medicalstore.pos.controller;

import com.medicalstore.pos.dto.request.CreatePacienteRequest;
import com.medicalstore.pos.dto.response.PacienteResponse;
import com.medicalstore.pos.repository.PacienteRepository;
import com.medicalstore.pos.repository.UsuarioRepository;
import com.medicalstore.pos.service.PacienteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/pacientes")
@Tag(name = "M04 Pacientes", description = "Registro de pacientes por DNI (RN05.0)")
public class PacienteController extends BaseController {
    
    private final PacienteService pacienteService;
    private final PacienteRepository pacienteRepository;
    
    public PacienteController(PacienteService pacienteService, 
                              UsuarioRepository usuarioRepository,
                              PacienteRepository pacienteRepository) {
        super(usuarioRepository);
        this.pacienteService = pacienteService;
        this.pacienteRepository = pacienteRepository;
    }
    
    @GetMapping
    @Operation(summary = "Listar pacientes activos")
    public ResponseEntity<List<PacienteResponse>> getAll() {
        return ResponseEntity.ok(pacienteService.getAll());
    }
    
    @GetMapping("/buscar")
    @Operation(summary = "Buscar paciente por DNI o nombre")
    public ResponseEntity<List<PacienteResponse>> buscar(@RequestParam String texto) {
        return ResponseEntity.ok(pacienteService.buscar(texto));
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Detalle de un paciente")
    public ResponseEntity<PacienteResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(pacienteService.getById(id));
    }
    
    @PostMapping
    @Operation(summary = "Registrar paciente")
    public ResponseEntity<PacienteResponse> create(
            @Valid @RequestBody CreatePacienteRequest request, HttpServletRequest httpRequest) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(pacienteService.create(request, currentUser(), httpRequest));
    }

    @GetMapping("/exists/{dni}")
    @Operation(summary = "Verificar si DNI existe en BD")
    public ResponseEntity<Map<String, Boolean>> existsDni(@PathVariable String dni) {
        boolean exists = pacienteRepository.existsByDni(dni);
        return ResponseEntity.ok(Map.of("exists", exists));
    }
}
