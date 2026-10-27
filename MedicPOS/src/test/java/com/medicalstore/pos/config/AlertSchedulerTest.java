package com.medicalstore.pos.config;

import com.medicalstore.pos.service.AlertaService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AlertSchedulerTest {

    @Mock
    private AlertaService alertaService;

    private AlertScheduler scheduler;

    @Test
    void evaluarAlertasDiarias_debeLlamarAlertaServiceYLogear() {
        // Given
        scheduler = new AlertScheduler(alertaService);
        when(alertaService.getAll()).thenReturn(java.util.Collections.emptyList());

        // When
        scheduler.evaluarAlertasDiarias();

        // Then
        verify(alertaService).getAll();
    }

    @Test
    void evaluarAlertasPostOperacion_debeLlamarAlertaServiceYLogear() {
        // Given
        scheduler = new AlertScheduler(alertaService);
        when(alertaService.getAll()).thenReturn(java.util.Collections.emptyList());

        // When
        scheduler.evaluarAlertasPostOperacion();

        // Then
        verify(alertaService).getAll();
    }

    @Test
    void evaluarAlertasDiarias_debeManejarExcepcionGracefully() {
        // Given
        scheduler = new AlertScheduler(alertaService);
        when(alertaService.getAll()).thenThrow(new RuntimeException("DB Error"));

        // When - no debe lanzar excepción
        scheduler.evaluarAlertasDiarias();

        // Then
        verify(alertaService).getAll();
    }
}