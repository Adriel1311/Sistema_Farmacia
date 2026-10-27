package com.medicalstore.pos.service;

import com.medicalstore.pos.entity.ConfiguracionSistema;
import com.medicalstore.pos.repository.ConfiguracionSistemaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConfiguracionServiceTest {

    @Mock
    private ConfiguracionSistemaRepository configuracionRepository;
    @Mock
    private com.medicalstore.pos.service.AuditoriaService auditoriaService;

    private ConfiguracionService configuracionService;

    @BeforeEach
    void setUp() {
        configuracionService = new ConfiguracionService(configuracionRepository, auditoriaService);
    }

    @Test
    void getInt_debeRetornarValorParseadoCuandoExiste() {
        // Given
        ConfiguracionSistema config = ConfiguracionSistema.builder()
                .clave("test.clave")
                .valor("42")
                .build();
        when(configuracionRepository.findByClave("test.clave")).thenReturn(Optional.of(config));

        // When
        int result = configuracionService.getInt("test.clave", 0);

        // Then
        assertThat(result).isEqualTo(42);
    }

    @Test
    void getInt_debeRetornarDefaultCuandoNoExiste() {
        // Given
        when(configuracionRepository.findByClave("inexistente")).thenReturn(Optional.empty());

        // When
        int result = configuracionService.getInt("inexistente", 100);

        // Then
        assertThat(result).isEqualTo(100);
    }

    @Test
    void getInt_debeRetornarDefaultCuandoValorNoEsNumerico() {
        // Given
        ConfiguracionSistema config = ConfiguracionSistema.builder()
                .clave("test.clave")
                .valor("no-numerico")
                .build();
        when(configuracionRepository.findByClave("test.clave")).thenReturn(Optional.of(config));

        // When
        int result = configuracionService.getInt("test.clave", 99);

        // Then
        assertThat(result).isEqualTo(99);
    }

    @Test
    void getBoolean_debeRetornarTrueCuandoValorEsTrue() {
        // Given
        ConfiguracionSistema config = ConfiguracionSistema.builder()
                .clave("test.bool")
                .valor("true")
                .build();
        when(configuracionRepository.findByClave("test.bool")).thenReturn(Optional.of(config));

        // When
        boolean result = configuracionService.getBoolean("test.bool", false);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void getBoolean_debeRetornarFalseCuandoValorEsFalse() {
        // Given
        ConfiguracionSistema config = ConfiguracionSistema.builder()
                .clave("test.bool")
                .valor("false")
                .build();
        when(configuracionRepository.findByClave("test.bool")).thenReturn(Optional.of(config));

        // When
        boolean result = configuracionService.getBoolean("test.bool", true);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void getBoolean_debeRetornarDefaultCuandoNoExiste() {
        // Given
        when(configuracionRepository.findByClave("inexistente")).thenReturn(Optional.empty());

        // When
        boolean result = configuracionService.getBoolean("inexistente", true);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void getDiasCritico_debeUsarCacheEnSegundaLlamada() {
        // Given
        ConfiguracionSistema config = ConfiguracionSistema.builder()
                .clave(ConfiguracionService.CLAVE_DIAS_CRITICO)
                .valor("25")
                .build();
        when(configuracionRepository.findByClave(ConfiguracionService.CLAVE_DIAS_CRITICO)).thenReturn(Optional.of(config));

        // When - primera llamada (va a BD)
        int result1 = configuracionService.getDiasCritico();
        // Segunda llamada (debe usar cache)
        int result2 = configuracionService.getDiasCritico();

        // Then
        assertThat(result1).isEqualTo(25);
        assertThat(result2).isEqualTo(25);
        // Verificar que solo se llamó una vez al repositorio
        verify(configuracionRepository, times(1)).findByClave(ConfiguracionService.CLAVE_DIAS_CRITICO);
    }

    @Test
    void getDiasProximo_debeRetornarDefaultCuandoNoConfigurado() {
        // Given
        when(configuracionRepository.findByClave(ConfiguracionService.CLAVE_DIAS_PROXIMO)).thenReturn(Optional.empty());

        // When
        int result = configuracionService.getDiasProximo();

        // Then
        assertThat(result).isEqualTo(ConfiguracionService.DEFAULT_DIAS_PROXIMO);
    }
}