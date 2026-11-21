package com.medicalstore.pos.security;

import com.medicalstore.pos.entity.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtTokenProvider {
    
    @Value("${spring.security.jwt.secret}")
    private String jwtSecret;
    
    @Value("${spring.security.jwt.expiration}")
    private long defaultJwtExpiration;
    
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }
    
    public String generateToken(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return buildToken(userDetails.getUsername(), null, defaultJwtExpiration);
    }
    
    /**
     * Genera el token de un usuario FarmaMed incorporando su rol (RN08).
     */
    public String generateToken(Usuario usuario) {
        return buildToken(usuario.getUsuario(), usuario.getNombreRol(), defaultJwtExpiration);
    }
    
    /**
     * Genera un token con expiración personalizada (en milisegundos).
     */
    public String generateToken(Usuario usuario, long expirationMs) {
        return buildToken(usuario.getUsuario(), usuario.getNombreRol(), expirationMs);
    }
    
    private String buildToken(String subject, String rol, long expirationMs) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationMs);
        
        var builder = Jwts.builder()
                .subject(subject)
                .issuedAt(now)
                .expiration(expiryDate);
        
        if (rol != null) {
            builder.claim("rol", rol);
        }
        
        return builder.signWith(getSigningKey()).compact();
    }
    
    public String getUsernameFromToken(String token) {
        return getClaims(token).getSubject();
    }
    
    /**
     * Rol declarado en el token. Se usa para autorizacion sin volver a la base.
     */
    public String getRolFromToken(String token) {
        Claims claims = getClaims(token);
        return claims.get("rol", String.class);
    }
    
    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
    
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
