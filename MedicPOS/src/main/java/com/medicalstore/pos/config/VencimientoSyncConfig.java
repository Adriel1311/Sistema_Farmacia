package com.medicalstore.pos.config;

import com.medicalstore.pos.service.LoteService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Marca como VENCIDO todo lote cuya fecha ya paso al arrancar la aplicacion (RN03).
 * Sin esto el lote seguiria ACTIVO hasta la primera peticion de inventario.
 */
@Configuration
public class VencimientoSyncConfig {
    
    private static final Logger log = LoggerFactory.getLogger(VencimientoSyncConfig.class);
    
    @Bean
    public ApplicationRunner sincronizarLotesVencidos(LoteService loteService) {
        return args -> {
            int sincronizados = loteService.sincronizarVencidos();
            if (sincronizados > 0) {
                log.info("FarmaMed: {} lote(s) marcados como VENCIDO (RN03)", sincronizados);
            }
        };
    }
}
