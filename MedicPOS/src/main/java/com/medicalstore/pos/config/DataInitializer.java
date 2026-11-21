package com.medicalstore.pos.config;

import com.medicalstore.pos.entity.Rol;
import com.medicalstore.pos.entity.Usuario;
import com.medicalstore.pos.repository.RolRepository;
import com.medicalstore.pos.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Crea los roles del diccionario de datos y un usuario de arranque por rol.
 * En un despliegue real las claves se cambian desde M09 Usuarios.
 */
@Configuration
public class DataInitializer {
    
    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);
    
    @Bean
    public CommandLineRunner initData(RolRepository rolRepository,
                                      UsuarioRepository usuarioRepository,
                                      PasswordEncoder passwordEncoder) {
        return args -> {
            Rol jefe = crearRol(rolRepository, "JEFE_FARMACIA",
                    "Responsable de la farmacia. Aprueba pedidos, configura plazos y unica que autoriza excepcion FEFO.");
            Rol tecnico = crearRol(rolRepository, "TECNICO_FARMACIA",
                    "Registra inventario y atiende en caja. Los avisos solo los consulta.");
            Rol logistica = crearRol(rolRepository, "LOGISTICA",
                    "Maneja proveedores y pedidos, y atiende los avisos de stock. No dispensa.");
            Rol adminTi = crearRol(rolRepository, "ADMIN_TI",
                    "Crea usuarios y gestiona respaldos. No vende en caja ni toca inventario.");
            
            String clave = passwordEncoder.encode("FarmaMed2026");
            
            crearUsuario(usuarioRepository, "jefe", "Elena", "Quispe Rojas", "jefe@farmamed.pe", jefe, clave);
            crearUsuario(usuarioRepository, "tecnico", "Luis", "Ramirez Vega", "tecnico@farmamed.pe", tecnico, clave);
            crearUsuario(usuarioRepository, "logistica", "Maria", "Chavez Soto", "logistica@farmamed.pe", logistica, clave);
            crearUsuario(usuarioRepository, "admin", "Carlos", "Torres Diaz", "admin@farmamed.pe", adminTi, clave);
            
            log.info("FarmaMed: roles y usuarios de arranque verificados");
        };
    }
    
    private Rol crearRol(RolRepository rolRepository, String nombre, String descripcion) {
        return rolRepository.findByNombre(nombre)
                .orElseGet(() -> rolRepository.save(Rol.builder()
                        .nombre(nombre)
                        .descripcion(descripcion)
                        .activo(true)
                        .build()));
    }
    
    private void crearUsuario(UsuarioRepository usuarioRepository,
                              String usuario, String nombres, String apellidos,
                              String correo, Rol rol, String claveHash) {
        Usuario usuarioExistente = usuarioRepository.findByUsuario(usuario).orElse(null);
        if (usuarioExistente != null) {
            // Actualizar rol si es diferente
            if (!rol.getNombre().equals(usuarioExistente.getNombreRol())) {
                usuarioExistente.setRol(rol);
                usuarioExistente.setPasswordHash(claveHash);
                usuarioExistente.setNombres(nombres);
                usuarioExistente.setApellidos(apellidos);
                usuarioExistente.setCorreo(correo);
                usuarioExistente.setActivo(true);
                usuarioRepository.save(usuarioExistente);
                log.info("FarmaMed: usuario existente actualizado -> {}", usuario);
            }
            return;
        }
        usuarioRepository.save(Usuario.builder()
                .usuario(usuario)
                .nombres(nombres)
                .apellidos(apellidos)
                .correo(correo)
                .passwordHash(claveHash)
                .rol(rol)
                .activo(true)
                .build());
    }
}
