package com.medicalstore.pos.repository;

import com.medicalstore.pos.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByUsuario(String usuario);
    boolean existsByUsuario(String usuario);
    boolean existsByCorreo(String correo);
    Optional<Usuario> findByCorreo(String correo);

    @Query("SELECT u FROM Usuario u JOIN FETCH u.rol WHERE u.usuario = :usuario")
    Optional<Usuario> findByUsuarioWithRol(@Param("usuario") String usuario);

    @Query("SELECT u FROM Usuario u JOIN FETCH u.rol ORDER BY u.activo DESC, u.apellidos ASC")
    List<Usuario> findAllWithRol();

    @Query("SELECT u FROM Usuario u JOIN FETCH u.rol WHERE u.rol.nombre = :rolNombre ORDER BY u.apellidos ASC")
    List<Usuario> findByRolNombre(@Param("rolNombre") String rolNombre);

    long countByRolNombreAndActivoTrue(String rolNombre);

    @Query("SELECT u FROM Usuario u JOIN FETCH u.rol WHERE u.ultimoAcceso >= :desde ORDER BY u.ultimoAcceso DESC")
    List<Usuario> findAccesosDesde(@Param("desde") LocalDateTime desde);
}
