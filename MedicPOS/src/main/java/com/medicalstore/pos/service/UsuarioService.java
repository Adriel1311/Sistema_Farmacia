package com.medicalstore.pos.service;

import com.medicalstore.pos.dto.request.ChangePasswordRequest;
import com.medicalstore.pos.dto.request.CreateUsuarioRequest;
import com.medicalstore.pos.dto.request.UpdateUsuarioRequest;
import com.medicalstore.pos.dto.response.UsuarioResponse;
import com.medicalstore.pos.entity.Auditoria;
import com.medicalstore.pos.entity.Rol;
import com.medicalstore.pos.entity.Usuario;
import com.medicalstore.pos.repository.RolRepository;
import com.medicalstore.pos.repository.UsuarioRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * M09 Usuarios / M10 Roles (RF-11, RN08).
 * Solo el Administrador TI administra las cuentas de acceso al sistema.
 */
@Service
public class UsuarioService {
    
    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditoriaService auditoriaService;
    
    public UsuarioService(UsuarioRepository usuarioRepository,
                          RolRepository rolRepository,
                          PasswordEncoder passwordEncoder,
                          AuditoriaService auditoriaService) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditoriaService = auditoriaService;
    }
    
    @Transactional(readOnly = true)
    public List<UsuarioResponse> getAll() {
        return usuarioRepository.findAllWithRol().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public UsuarioResponse getById(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario not found with id: " + id));
        return mapToResponse(usuario);
    }
    
    @Transactional
    public UsuarioResponse create(CreateUsuarioRequest request, Usuario user, HttpServletRequest httpRequest) {
        if (usuarioRepository.existsByUsuario(request.getUsuario())) {
            throw new RuntimeException("El usuario " + request.getUsuario() + " ya existe");
        }
        if (usuarioRepository.existsByCorreo(request.getCorreo())) {
            throw new RuntimeException("El correo " + request.getCorreo() + " ya esta registrado");
        }
        
        Rol rol = rolRepository.findById(request.getRolId())
                .orElseThrow(() -> new RuntimeException("Rol not found with id: " + request.getRolId()));
        
        Usuario usuario = Usuario.builder()
                .usuario(request.getUsuario())
                .nombres(request.getNombres())
                .apellidos(request.getApellidos())
                .correo(request.getCorreo())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .rol(rol)
                .activo(true)
                .createdBy(user != null ? user.getId() : null)
                .build();
        
        usuario = usuarioRepository.save(usuario);
        
        auditoriaService.log(Auditoria.Accion.USUARIO_CREADO, user, "Usuario", usuario.getId().toString(),
                "Usuario creado: " + usuario.getUsuario(), null, usuario.toString(), httpRequest);
        
        return mapToResponse(usuario);
    }
    
    @Transactional
    public UsuarioResponse update(Long id, UpdateUsuarioRequest request, Usuario user, HttpServletRequest httpRequest) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario not found with id: " + id));
        
        String valorAnterior = usuario.toString();
        
        if (!usuario.getCorreo().equals(request.getCorreo()) && usuarioRepository.existsByCorreo(request.getCorreo())) {
            throw new RuntimeException("El correo " + request.getCorreo() + " ya esta registrado");
        }
        
        Rol rol = rolRepository.findById(request.getRolId())
                .orElseThrow(() -> new RuntimeException("Rol not found with id: " + request.getRolId()));
        
        // Evita que el sistema se quede sin ningun administrador de TI activo
        if (!usuario.getActivo() && request.getActivo()) {
            validarNoDejarSinAdminTi(usuario);
        }
        
        usuario.setNombres(request.getNombres());
        usuario.setApellidos(request.getApellidos());
        usuario.setCorreo(request.getCorreo());
        usuario.setRol(rol);
        usuario.setActivo(request.getActivo());
        usuario.setUpdatedBy(user != null ? user.getId() : null);
        
        usuario = usuarioRepository.save(usuario);
        
        auditoriaService.log(Auditoria.Accion.USUARIO_ACTUALIZADO, user, "Usuario", id.toString(),
                "Usuario actualizado: " + usuario.getUsuario(), valorAnterior, usuario.toString(), httpRequest);
        
        return mapToResponse(usuario);
    }
    
    @Transactional
    public UsuarioResponse toggleActivo(Long id, Usuario user, HttpServletRequest httpRequest) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario not found with id: " + id));
        
        boolean nuevoEstado = !usuario.getActivo();
        if (nuevoEstado) {
            validarNoDejarSinAdminTi(usuario);
        }
        
        usuario.setActivo(nuevoEstado);
        usuario.setUpdatedBy(user != null ? user.getId() : null);
        usuario = usuarioRepository.save(usuario);
        
        auditoriaService.log(
                nuevoEstado ? Auditoria.Accion.USUARIO_ACTIVADO : Auditoria.Accion.USUARIO_DESACTIVADO,
                user, "Usuario", id.toString(),
                (nuevoEstado ? "Usuario activado: " : "Usuario desactivado: ") + usuario.getUsuario(),
                null, nuevoEstado ? "activo" : "inactivo", httpRequest);
        
        return mapToResponse(usuario);
    }
    
    @Transactional
    public void changePassword(Long id, ChangePasswordRequest request, Usuario user, HttpServletRequest httpRequest) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario not found with id: " + id));
        
        if (!passwordEncoder.matches(request.getPasswordActual(), usuario.getPasswordHash())) {
            throw new RuntimeException("La contrasena actual no es correcta");
        }
        
        usuario.setPasswordHash(passwordEncoder.encode(request.getPasswordNuevo()));
        usuario.setUpdatedBy(user != null ? user.getId() : null);
        usuarioRepository.save(usuario);
        
        auditoriaService.log(Auditoria.Accion.CONTRASENA_ACTUALIZADA, user, "Usuario", id.toString(),
                "Contrasena actualizada: " + usuario.getUsuario(), null, "********", httpRequest);
    }
    
    /**
     * El sistema debe conservar al menos un Administrador TI activo.
     */
    private void validarNoDejarSinAdminTi(Usuario usuario) {
        if ("ADMIN_TI".equals(usuario.getNombreRol())
                && usuarioRepository.countByRolNombreAndActivoTrue("ADMIN_TI") <= 1) {
            throw new RuntimeException("No se puede desactivar al unico Administrador TI activo");
        }
    }
    
    @Transactional
    public void registrarAcceso(Usuario usuario) {
        usuario.setUltimoAcceso(LocalDateTime.now());
        usuarioRepository.save(usuario);
    }
    
    private UsuarioResponse mapToResponse(Usuario u) {
        return UsuarioResponse.builder()
                .id(u.getId())
                .usuario(u.getUsuario())
                .nombres(u.getNombres())
                .apellidos(u.getApellidos())
                .fullName(u.getNombreCompleto())
                .correo(u.getCorreo())
                .rolId(u.getRol() != null ? u.getRol().getId() : null)
                .rolNombre(u.getNombreRol())
                .activo(u.getActivo())
                .ultimoAcceso(u.getUltimoAcceso())
                .createdAt(u.getCreatedAt())
                .updatedAt(u.getUpdatedAt())
                .build();
    }
}
