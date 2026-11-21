package com.medicalstore.pos.controller;

import com.medicalstore.pos.dto.response.RolResponse;
import com.medicalstore.pos.entity.Rol;
import com.medicalstore.pos.repository.RolRepository;
import com.medicalstore.pos.repository.UsuarioRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * M10 Roles: catalogo cerrado de FarmaMed. Los roles no se crean ni se editan desde la API.
 */
@RestController
@RequestMapping("/api/roles")
@Tag(name = "M10 Roles", description = "Catalogo de roles del diccionario de datos")
public class RolController extends BaseController {
    
    private final RolRepository rolRepository;
    private final UsuarioRepository usuarioRepository;
    
    public RolController(RolRepository rolRepository, UsuarioRepository usuarioRepository) {
        super(usuarioRepository);
        this.rolRepository = rolRepository;
        this.usuarioRepository = usuarioRepository;
    }
    
    @GetMapping
    @Operation(summary = "Listar roles")
    public ResponseEntity<List<RolResponse>> getAll() {
        List<RolResponse> roles = rolRepository.findByActivoTrueOrderByNombreAsc().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(roles);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Detalle de un rol")
    public ResponseEntity<RolResponse> getById(@PathVariable Long id) {
        Rol rol = rolRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Rol not found with id: " + id));
        return ResponseEntity.ok(mapToResponse(rol));
    }
    
    private RolResponse mapToResponse(Rol rol) {
        return RolResponse.builder()
                .id(rol.getId())
                .nombre(rol.getNombre())
                .descripcion(rol.getDescripcion())
                .activo(rol.getActivo())
                .usuariosAsignados(usuarioRepository.countByRolNombreAndActivoTrue(rol.getNombre()))
                .createdAt(rol.getCreatedAt())
                .build();
    }
}
