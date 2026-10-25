package com.medicalstore.pos.service;

import com.medicalstore.pos.dto.request.UpdateConfiguracionRequest;
import com.medicalstore.pos.dto.response.ConfiguracionResponse;
import com.medicalstore.pos.entity.Auditoria;
import com.medicalstore.pos.entity.ConfiguracionSistema;
import com.medicalstore.pos.entity.Usuario;
import com.medicalstore.pos.repository.ConfiguracionSistemaRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * M11 Configuracion: parametros globales del sistema.
 * Solo el Jefe de Farmacia puede modificarlos y todo cambio queda auditado (RN04, RN06).
 */
@Service
public class ConfiguracionService {
    
    // Claves de parametros globales
    public static final String CLAVE_DIAS_CRITICO = "vencimiento.dias.critico";
    public static final String CLAVE_DIAS_PROXIMO = "vencimiento.dias.proximo";
    public static final String CLAVE_TIMEOUT_SESION = "sesion.timeout.minutos";
    public static final String CLAVE_HORA_EVALUACION = "alertas.hora.evaluacion";
    public static final String CLAVE_ORDEN_SUGERIDA_AUTO = "orden.sugerida.automatica";
    public static final String CLAVE_PERMITIR_EXCEPCION_FEFO = "fefo.permitir.excepcion";
    
    // Valores por defecto (RN04: umbrales 30/60 validados con el docente)
    public static final int DEFAULT_DIAS_CRITICO = 30;
    public static final int DEFAULT_DIAS_PROXIMO = 60;
    
    private final ConfiguracionSistemaRepository configuracionRepository;
    private final AuditoriaService auditoriaService;
    
    public ConfiguracionService(ConfiguracionSistemaRepository configuracionRepository,
                                AuditoriaService auditoriaService) {
        this.configuracionRepository = configuracionRepository;
        this.auditoriaService = auditoriaService;
    }
    
    /**
     * Devuelve el valor numerico de un parametro o el valor por defecto si no existe.
     */
    @Transactional(readOnly = true)
    public int getInt(String clave, int porDefecto) {
        return configuracionRepository.findByClave(clave)
                .map(c -> {
                    try {
                        return Integer.parseInt(c.getValor().trim());
                    } catch (NumberFormatException e) {
                        return porDefecto;
                    }
                })
                .orElse(porDefecto);
    }
    
    @Transactional(readOnly = true)
    public boolean getBoolean(String clave, boolean porDefecto) {
        return configuracionRepository.findByClave(clave)
                .map(c -> Boolean.parseBoolean(c.getValor().trim()))
                .orElse(porDefecto);
    }
    
    /**
     * Dias del semaforo critico (ROJO). RN04 validado en 30.
     */
    @Transactional(readOnly = true)
    public int getDiasCritico() {
        return getInt(CLAVE_DIAS_CRITICO, DEFAULT_DIAS_CRITICO);
    }
    
    /**
     * Dias del semaforo proximo a vencer (AMBAR). RN04 validado en 60.
     */
    @Transactional(readOnly = true)
    public int getDiasProximo() {
        return getInt(CLAVE_DIAS_PROXIMO, DEFAULT_DIAS_PROXIMO);
    }
    
    @Transactional(readOnly = true)
    public List<ConfiguracionResponse> getAll() {
        return configuracionRepository.findAllByOrderByClaveAsc().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional
    public List<ConfiguracionResponse> update(UpdateConfiguracionRequest request, Usuario user, HttpServletRequest httpRequest) {
        List<ConfiguracionResponse> actualizados = new java.util.ArrayList<>();
        
        for (Map.Entry<String, String> entry : request.getValores().entrySet()) {
            String clave = entry.getKey();
            String valor = entry.getValue();
            
            validar(clave, valor);
            
            ConfiguracionSistema config = configuracionRepository.findByClave(clave)
                    .orElseGet(() -> ConfiguracionSistema.builder()
                            .clave(clave)
                            .descripcion(clave)
                            .activo(true)
                            .build());
            
            String valorAnterior = config.getValor();
            config.setValor(valor);
            config.setUpdatedBy(user != null ? user.getId() : null);
            ConfiguracionSistema guardada = configuracionRepository.save(config);
            
            auditoriaService.log(Auditoria.Accion.CONFIGURACION_ACTUALIZADA, user, "Configuracion", 
                    clave, "Parametro actualizado: " + clave,
                    valorAnterior, valor, httpRequest);
            
            actualizados.add(mapToResponse(guardada));
        }
        
        return actualizados;
    }
    
    private void validar(String clave, String valor) {
        if (valor == null || valor.trim().isEmpty()) {
            throw new RuntimeException("El valor del parametro " + clave + " no puede estar vacio");
        }
        if (CLAVE_DIAS_CRITICO.equals(clave) || CLAVE_DIAS_PROXIMO.equals(clave) || CLAVE_TIMEOUT_SESION.equals(clave)) {
            try {
                int valorNumerico = Integer.parseInt(valor.trim());
                if (valorNumerico <= 0) {
                    throw new RuntimeException("El parametro " + clave + " debe ser mayor a cero");
                }
            } catch (NumberFormatException e) {
                throw new RuntimeException("El parametro " + clave + " debe ser numerico");
            }
        }
        if (CLAVE_DIAS_CRITICO.equals(clave) && Integer.parseInt(valor.trim()) >= getDiasProximo()) {
            throw new RuntimeException("El umbral critico debe ser menor que el umbral de proximo a vencer");
        }
    }
    
    private ConfiguracionResponse mapToResponse(ConfiguracionSistema c) {
        return ConfiguracionResponse.builder()
                .id(c.getId())
                .clave(c.getClave())
                .valor(c.getValor())
                .descripcion(c.getDescripcion())
                .activo(c.getActivo())
                .categoria(categoriaDe(c.getClave()))
                .tipoDato(tipoDatoDe(c.getValor()))
                .updatedAt(c.getUpdatedAt())
                .build();
    }
    
    private String categoriaDe(String clave) {
        if (clave.startsWith("vencimiento")) return "Semaforo de vencimiento";
        if (clave.startsWith("sesion")) return "Seguridad";
        if (clave.startsWith("alertas")) return "Alertas";
        if (clave.startsWith("orden")) return "Abastecimiento";
        if (clave.startsWith("fefo")) return "Dispensacion";
        return "General";
    }
    
    private String tipoDatoDe(String valor) {
        if (valor == null) return "TEXTO";
        if (valor.equalsIgnoreCase("true") || valor.equalsIgnoreCase("false")) return "BOOLEANO";
        try {
            Integer.parseInt(valor.trim());
            return "NUMERO";
        } catch (NumberFormatException e) {
            return "TEXTO";
        }
    }
    
    /**
     * Mapa de umbrales vigente, util para respuestas que incluyen el semaforo aplicado.
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getSemaforoVigente() {
        Map<String, Object> semaforo = new LinkedHashMap<>();
        semaforo.put("diasCritico", getDiasCritico());
        semaforo.put("diasProximo", getDiasProximo());
        return semaforo;
    }
}
