package com.medicalstore.pos.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.medicalstore.pos.client.FactilizaClient;
import com.medicalstore.pos.dto.response.DniResponse;
import com.medicalstore.pos.repository.PacienteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class DniService {
    private final PacienteRepository pacienteRepository;
    private final FactilizaClient factilizaClient;

    @Value("${factiliza.mock-enabled:true}")
    private boolean mockEnabled;

    private final Cache<String, DniResponse> dniCache = Caffeine.newBuilder()
            .maximumSize(10_000)
            .expireAfterWrite(24, TimeUnit.HOURS)
            .recordStats()
            .build();

    // Datos mock para desarrollo local - DNI reales de prueba
    private static final Map<String, DniResponse> MOCK_DNI_DATA = new ConcurrentHashMap<>(Map.ofEntries(
        Map.entry("72953440", buildMock("72953440", "JUAN CARLOS", "PEREZ GOMEZ")),
        Map.entry("12345678", buildMock("12345678", "MARIA ELENA", "RODRIGUEZ LOPEZ")),
        Map.entry("87654321", buildMock("87654321", "CARLOS ALBERTO", "GONZALEZ MARTINEZ")),
        Map.entry("11111111", buildMock("11111111", "ANA LUCIA", "FERNANDEZ RUIZ")),
        Map.entry("22222222", buildMock("22222222", "LUIS MIGUEL", "TORRES VARGAS")),
        Map.entry("33333333", buildMock("33333333", "CARMEN ROSA", "CASTRO MENDOZA")),
        Map.entry("44444444", buildMock("44444444", "ROBERTO CARLOS", "SILVA ORTEGA")),
        Map.entry("55555555", buildMock("55555555", "PATRICIA BEATRIZ", "RAMIREZ HERRERA")),
        Map.entry("66666666", buildMock("66666666", "FERNANDO JOSE", "MENDOZA CASTILLO")),
        Map.entry("77777777", buildMock("77777777", "SOFIA ALEJANDRA", "JIMENEZ MORALES"))
    ));

    private static DniResponse buildMock(String dni, String nombres, String apellidos) {
        DniResponse r = new DniResponse();
        r.setDni(dni);
        r.setNombres(nombres);
        r.setApellidos(apellidos);
        r.setNombreCompleto(nombres + " " + apellidos);
        r.setDireccion("AV. PRUEBA 123 - LIMA");
        return r;
    }

    public DniResponse consultarDni(String dni) {
        if (dni == null || !dni.matches("^\\d{8}$")) {
            throw new IllegalArgumentException("DNI inválido: debe tener 8 dígitos");
        }

        // 1. Check BD: ¿DNI ya existe en nuestro sistema?
        if (pacienteRepository.existsByDni(dni)) {
            throw new DniYaRegistradoException("El DNI " + dni + " ya está registrado en el sistema");
        }

        // 2. Check cache
        DniResponse cached = dniCache.getIfPresent(dni);
        if (cached != null) {
            log.debug("DNI {} encontrado en cache", dni);
            return cached;
        }

        // 3. Llamar a Factiliza
        log.info("Consultando DNI {} en Factiliza", dni);
        try {
            DniResponse response = factilizaClient.consultarDni(dni).block();
            
            if (response != null) {
                // Normalizar: nombres y apellidos en formato proper case
                response.setNombres(formatoPropio(response.getNombres()));
                response.setApellidos(formatoPropio(response.getApellidos()));
                if (response.getNombreCompleto() != null) {
                    response.setNombreCompleto(formatoPropio(response.getNombreCompleto()));
                }
                
                dniCache.put(dni, response);
                log.info("DNI {} consultado y cacheado exitosamente", dni);
                return response;
            }
            throw new FactilizaException("Respuesta vacía de Factiliza", 502);
        } catch (FactilizaClient.FactilizaException e) {
            log.warn("Error consultando Factiliza para DNI {}: {}", dni, e.getMessage());
            
            // Si mock está habilitado, intentar devolver dato mock
            if (mockEnabled) {
                return getMockOrThrow(dni, e);
            }
            throw e;
        } catch (Exception e) {
            log.error("Error inesperado consultando DNI {}: {}", dni, e.getMessage());
            
            // Si mock está habilitado, intentar devolver dato mock
            if (mockEnabled) {
                return getMockOrThrow(dni, new FactilizaClient.FactilizaException("Error de conexión", 504));
            }
            throw new FactilizaException("Error de conexión con servicio de consulta DNI", 504);
        }
    }

    private DniResponse getMockOrThrow(String dni, Exception originalError) {
        DniResponse mock = MOCK_DNI_DATA.get(dni);
        if (mock != null) {
            log.info("MOCK MODE: Devolviendo dato simulado para DNI {}", dni);
            dniCache.put(dni, mock);
            return mock;
        }
        
        // Si no hay mock para este DNI, loggeamos y lanzamos error original
        log.warn("MOCK MODE: No hay dato simulado para DNI {}. Error original: {}", dni, originalError.getMessage());
        throw new FactilizaClient.FactilizaException("DNI no encontrado en modo mock (use DNI de prueba: 72953440, 12345678, etc.)", 404);
    }

    public boolean existeEnSistema(String dni) {
        return pacienteRepository.existsByDni(dni);
    }

    private String formatoPropio(String texto) {
        if (texto == null || texto.isBlank()) {
            return texto;
        }
        String[] palabras = texto.toLowerCase().split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < palabras.length; i++) {
            if (i > 0) sb.append(' ');
            String p = palabras[i];
            if (p.length() > 0) {
                sb.append(Character.toUpperCase(p.charAt(0)));
                if (p.length() > 1) sb.append(p.substring(1));
            }
        }
        return sb.toString();
    }

    public static class FactilizaException extends RuntimeException {
        private final int statusCode;

        public FactilizaException(String message, int statusCode) {
            super(message);
            this.statusCode = statusCode;
        }

        public int getStatusCode() {
            return statusCode;
        }
    }

    public static class DniYaRegistradoException extends RuntimeException {
        public DniYaRegistradoException(String message) {
            super(message);
        }
    }
}