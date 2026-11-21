package com.medicalstore.pos.controller;

import com.medicalstore.pos.dto.response.AuthResponse;
import com.medicalstore.pos.entity.Usuario;
import com.medicalstore.pos.repository.UsuarioRepository;
import com.medicalstore.pos.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.medicalstore.pos.dto.request.LoginRequest;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "M01 Autenticacion", description = "Inicio y cierre de sesion (RN08)")
public class AuthController extends BaseController {
    
    private final AuthService authService;
    
    public AuthController(AuthService authService, UsuarioRepository usuarioRepository) {
        super(usuarioRepository);
        this.authService = authService;
    }
    
    @PostMapping("/login")
    @Operation(summary = "Iniciar sesion", description = "Autentica al usuario y devuelve el token JWT")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        return ResponseEntity.ok(authService.login(request, httpRequest));
    }
    
    @GetMapping("/me")
    @Operation(summary = "Usuario de la sesion", description = "Perfil del usuario autenticado")
    public ResponseEntity<AuthResponse> me(@AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(authService.getCurrentUser(usuario.getUsuario()));
    }
    
    @PostMapping("/logout")
    @Operation(summary = "Cerrar sesion", description = "Registra el cierre de sesion en auditoria")
    public ResponseEntity<Void> logout(HttpServletRequest httpRequest) {
        authService.logout(httpRequest);
        return ResponseEntity.ok().build();
    }
}
