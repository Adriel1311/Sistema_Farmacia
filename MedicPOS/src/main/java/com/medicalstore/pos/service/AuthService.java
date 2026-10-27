package com.medicalstore.pos.service;

import com.medicalstore.pos.service.ConfiguracionService;
import com.medicalstore.pos.dto.request.LoginRequest;
import com.medicalstore.pos.dto.response.AuthResponse;
import com.medicalstore.pos.entity.Auditoria;
import com.medicalstore.pos.entity.Usuario;
import com.medicalstore.pos.repository.UsuarioRepository;
import com.medicalstore.pos.security.JwtTokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

/**
 * M01 Autenticacion (RF-12, RN08). Sesion unica por usuario dentro de la farmacia.
 * La expiración del token se toma de la configuración del sistema (sesion.timeout.minutos).
 */
@Service
public class AuthService {
    
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UsuarioRepository usuarioRepository;
    private final UsuarioService usuarioService;
    private final AuditoriaService auditoriaService;
    private final ConfiguracionService configuracionService;
    
    public AuthService(AuthenticationManager authenticationManager,
                       JwtTokenProvider jwtTokenProvider,
                       UsuarioRepository usuarioRepository,
                       UsuarioService usuarioService,
                       AuditoriaService auditoriaService,
                       ConfiguracionService configuracionService) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
        this.usuarioRepository = usuarioRepository;
        this.usuarioService = usuarioService;
        this.auditoriaService = auditoriaService;
        this.configuracionService = configuracionService;
    }
    
    @Transactional
    public AuthResponse login(LoginRequest request, HttpServletRequest httpRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsuario(), request.getPassword()));
        
        SecurityContextHolder.getContext().setAuthentication(authentication);
        
        Usuario usuario = usuarioRepository.findByUsuarioWithRol(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Usuario not found: " + request.getUsuario()));
        
        // Trazabilidad del ultimo acceso (M09)
        usuarioService.registrarAcceso(usuario);
        
        // RNF-07: expiración de sesión configurable (default 30 min)
        int timeoutMinutos = configuracionService.getInt(ConfiguracionService.CLAVE_TIMEOUT_SESION, 30);
        long expirationMs = TimeUnit.MINUTES.toMillis(timeoutMinutos);
        
        return mapToResponse(usuario, jwtTokenProvider.generateToken(usuario, expirationMs));
    }
    
    @Transactional(readOnly = true)
    public AuthResponse getCurrentUser(String username) {
        Usuario usuario = usuarioRepository.findByUsuarioWithRol(username)
                .orElseThrow(() -> new RuntimeException("Usuario not found: " + username));
        return mapToResponse(usuario, null);
    }
    
    @Transactional
    public void logout(HttpServletRequest httpRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return;
        }
        
        Usuario usuario = usuarioRepository.findByUsuarioWithRol(authentication.getName()).orElse(null);
        auditoriaService.log(Auditoria.Accion.USUARIO_ACTUALIZADO, usuario, "Usuario",
                usuario != null ? usuario.getId().toString() : authentication.getName(),
                "Cierre de sesion", null, null, httpRequest);
    }
    
    private AuthResponse mapToResponse(Usuario usuario, String token) {
        return AuthResponse.builder()
                .userId(usuario.getId())
                .username(usuario.getUsuario())
                .nombres(usuario.getNombres())
                .apellidos(usuario.getApellidos())
                .fullName(usuario.getNombreCompleto())
                .email(usuario.getCorreo())
                .role(usuario.getNombreRol())
                .token(token)
                .build();
    }
}
