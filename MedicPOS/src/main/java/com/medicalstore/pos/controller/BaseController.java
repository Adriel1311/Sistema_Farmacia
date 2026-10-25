package com.medicalstore.pos.controller;

import com.medicalstore.pos.entity.Usuario;
import com.medicalstore.pos.repository.UsuarioRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Resuelve el Usuario autenticado a partir del SecurityContext.
 * Evita repetir la busqueda en cada controlador.
 */
public abstract class BaseController {
    
    protected final UsuarioRepository usuarioRepository;
    
    protected BaseController(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }
    
    /**
     * @return el usuario de la sesion, o null si la peticion no tiene sesion.
     */
    protected Usuario currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        String username = authentication.getName();
        if (username == null || "anonymousUser".equals(username)) {
            return null;
        }
        return usuarioRepository.findByUsuarioWithRol(username).orElse(null);
    }
}
