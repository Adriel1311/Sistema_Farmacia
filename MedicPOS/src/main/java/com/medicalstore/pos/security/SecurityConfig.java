package com.medicalstore.pos.security;

import com.medicalstore.pos.config.RateLimitFilter;
import com.medicalstore.pos.config.SecurityHeadersFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * Autorizacion por rol segun el diccionario de datos de FarmaMed.
 *
 * JEFE_FARMACIA    todo, configura plazos y minimos, aprueba pedidos, unica que
 *                  autoriza excepcion FEFO.
 * TECNICO_FARMACIA registra inventario y atiende en caja, los avisos solo los ve.
 * LOGISTICA        proveedores y pedidos, atiende avisos de stock, no dispensa.
 * ADMIN_TI         usuarios y respaldos, no vende en caja ni toca inventario.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    
    private static final String JEFE = "JEFE_FARMACIA";
    private static final String TECNICO = "TECNICO_FARMACIA";
    private static final String LOGISTICA = "LOGISTICA";
    private static final String ADMIN_TI = "ADMIN_TI";
    
    private final UserDetailsService userDetailsService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint authenticationEntryPoint;
    private final RateLimitFilter rateLimitFilter;
    private final SecurityHeadersFilter securityHeadersFilter;
    
    /**
     * Origenes autorizados para el frontend. Por defecto solo el de desarrollo
     * (Angular en el puerto 4200). Se amplia con CORS_ORIGINS al desplegar.
     */
    @Value("${farmamed.cors.origins:http://localhost:4200}")
    private String[] origenesPermitidos;
    
    public SecurityConfig(UserDetailsService userDetailsService,
                          JwtAuthenticationFilter jwtAuthenticationFilter,
                          JwtAuthenticationEntryPoint authenticationEntryPoint,
                          RateLimitFilter rateLimitFilter,
                          SecurityHeadersFilter securityHeadersFilter) {
        this.userDetailsService = userDetailsService;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.authenticationEntryPoint = authenticationEntryPoint;
        this.rateLimitFilter = rateLimitFilter;
        this.securityHeadersFilter = securityHeadersFilter;
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }
    
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
    
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(origenesPermitidos));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(ex -> ex.authenticationEntryPoint(authenticationEntryPoint))
            // Add security headers filter first
            .addFilterBefore(securityHeadersFilter, UsernamePasswordAuthenticationFilter.class)
            // Add rate limiting filter
            .addFilterBefore(rateLimitFilter, UsernamePasswordAuthenticationFilter.class)
            .authorizeHttpRequests(auth -> auth
                // Publicos
                .requestMatchers("/api/auth/login").permitAll()
                .requestMatchers("/api/auth/logout").permitAll()
                .requestMatchers("/api/dni/**").permitAll()
                .requestMatchers(
                    "/swagger-ui.html",
                    "/swagger-ui/**",
                    "/v3/api-docs/**",
                    "/api-docs/**",
                    "/swagger-resources/**",
                    "/webjars/**",
                    "/error"
                ).permitAll()
                
                // Configuración: lectura de parámetro fefo.permitir.excepcion para dispensación
                .requestMatchers(HttpMethod.GET, "/api/configuracion/fefo.permitir.excepcion").hasAnyRole(JEFE, TECNICO, LOGISTICA, ADMIN_TI)
                
                // M11 Configuracion: Jefe y Admin TI cambian parametros (RN04, RN06)
                .requestMatchers("/api/configuracion/**").hasAnyRole(JEFE, ADMIN_TI)
                
                // M09 Usuarios y M10 Roles: solo Administrador TI (RN08)
                .requestMatchers("/api/usuarios/**").hasRole(ADMIN_TI)
                .requestMatchers("/api/roles/**").hasRole(ADMIN_TI)
                // Medicos: Jefe/Admin TI gestionan el catalogo; Tecnico solo consulta para recetas.
                .requestMatchers(HttpMethod.GET, "/api/medicos/**").hasAnyRole(JEFE, TECNICO, ADMIN_TI)
                .requestMatchers("/api/medicos/**").hasAnyRole(JEFE, ADMIN_TI)
                // M15 Auditoria: lectura y escritura para Jefe de Farmacia y Administrador TI
                .requestMatchers(HttpMethod.GET, "/api/auditoria/**").hasAnyRole(JEFE, ADMIN_TI)
                .requestMatchers("/api/auditoria/**").hasAnyRole(JEFE, ADMIN_TI)
                
                // M06 Abastecimiento: Logistica gestiona proveedores y pedidos (RN07), Admin TI acceso completo
                .requestMatchers(HttpMethod.GET, "/api/proveedores/**").hasAnyRole(JEFE, LOGISTICA, TECNICO, ADMIN_TI)
                .requestMatchers("/api/proveedores/**").hasAnyRole(LOGISTICA, ADMIN_TI)
                // Aprobar o rechazar una orden es potestad del Jefe de Farmacia.
                // Debe declararse ANTES del matcher general de ordenes o nunca se evalua.
                .requestMatchers(HttpMethod.PATCH, "/api/ordenes/*/resolver").hasRole(JEFE)
                .requestMatchers(HttpMethod.POST, "/api/ordenes/*/resolver").hasRole(JEFE)
                .requestMatchers(HttpMethod.GET, "/api/ordenes/**").hasAnyRole(JEFE, LOGISTICA, ADMIN_TI)
                .requestMatchers("/api/ordenes/**").hasAnyRole(LOGISTICA, ADMIN_TI)
                
                // M02 Medicamentos: el Jefe registra y modifica (RN07), Admin TI acceso completo
                .requestMatchers(HttpMethod.GET, "/api/medicamentos/**").hasAnyRole(JEFE, TECNICO, LOGISTICA, ADMIN_TI)
                .requestMatchers("/api/medicamentos/**").hasAnyRole(JEFE, ADMIN_TI)
                
                // M03 Inventario y lotes: Jefe, Tecnico y Admin TI acceso completo
                .requestMatchers("/api/lotes/**").hasAnyRole(JEFE, TECNICO, ADMIN_TI)
                .requestMatchers("/api/bajas/**").hasAnyRole(JEFE, TECNICO, ADMIN_TI)
                .requestMatchers("/api/movimientos/**").hasAnyRole(JEFE, TECNICO, ADMIN_TI)
                
                // M04 Dispensacion: atencion en caja. Admin TI acceso completo para pruebas
                .requestMatchers("/api/dispensaciones/**").hasAnyRole(JEFE, TECNICO, ADMIN_TI)
                .requestMatchers("/api/recetas/**").hasAnyRole(JEFE, TECNICO, ADMIN_TI)
                .requestMatchers("/api/pacientes/**").hasAnyRole(JEFE, TECNICO, ADMIN_TI)
                
                // M05 Alertas: el Tecnico solo consulta, Jefe, Logistic y Admin TI atienden
                .requestMatchers(HttpMethod.GET, "/api/alertas/**").hasAnyRole(JEFE, TECNICO, LOGISTICA, ADMIN_TI)
                .requestMatchers("/api/alertas/**").hasAnyRole(JEFE, LOGISTICA, ADMIN_TI)
                
                // M07 Dashboard y M08 Reportes
                .requestMatchers("/api/dashboard/**").hasAnyRole(JEFE, TECNICO, LOGISTICA, ADMIN_TI)
                .requestMatchers("/api/reportes/**").hasAnyRole(JEFE, LOGISTICA, ADMIN_TI)
                
                // Etiquetas de codigo de barras y QR: las imprime quien atiende en caja o inventario
                .requestMatchers("/api/codigos-barras/**").hasAnyRole(JEFE, TECNICO, LOGISTICA, ADMIN_TI)
                
                .anyRequest().authenticated()
            )
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
}
