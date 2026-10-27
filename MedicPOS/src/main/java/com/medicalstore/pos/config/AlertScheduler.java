package com.medicalstore.pos.config;

import com.medicalstore.pos.service.AlertaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
public class AlertScheduler {

    private static final Logger log = LoggerFactory.getLogger(AlertScheduler.class);

    private final AlertaService alertaService;

    public AlertScheduler(AlertaService alertaService) {
        this.alertaService = alertaService;
    }

    /**
     * RN05.2: Las alertas se revisan cada día a las 07:00.
     * Calcula las alertas y registra en auditoría si hay críticas nuevas.
     */
    @Scheduled(cron = "0 0 7 * * *", zone = "America/Lima")
    public void evaluarAlertasDiarias() {
        try {
            var alertas = alertaService.getAll();
            long altaPrioridad = alertas.stream()
                    .filter(a -> a.getPrioridad() == com.medicalstore.pos.dto.response.AlertaResponse.Prioridad.ALTA)
                    .filter(a -> !Boolean.TRUE.equals(a.getAtendida()))
                    .count();

            if (altaPrioridad > 0) {
                log.info("Evaluación diaria 07:00 - {} alertas de alta prioridad pendientes", altaPrioridad);
            } else {
                log.info("Evaluación diaria 07:00 - Sin alertas críticas pendientes");
            }
        } catch (Exception e) {
            log.error("Error en evaluación diaria de alertas", e);
        }
    }

    /**
     * RN05.2: También se revisan después de cada venta o ingreso.
     * Este método puede ser llamado programáticamente desde los services.
     */
    public void evaluarAlertasPostOperacion() {
        try {
            var alertas = alertaService.getAll();
            long altaPrioridad = alertas.stream()
                    .filter(a -> a.getPrioridad() == com.medicalstore.pos.dto.response.AlertaResponse.Prioridad.ALTA)
                    .filter(a -> !Boolean.TRUE.equals(a.getAtendida()))
                    .count();

            if (altaPrioridad > 0) {
                log.info("Evaluación post-operación - {} alertas de alta prioridad pendientes", altaPrioridad);
            }
        } catch (Exception e) {
            log.error("Error en evaluación post-operación de alertas", e);
        }
    }
}