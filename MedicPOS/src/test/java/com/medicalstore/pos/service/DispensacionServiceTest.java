package com.medicalstore.pos.service;

import com.medicalstore.pos.entity.Lote;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DispensacionServiceTest {

    @Mock
    private ConfiguracionService configuracionService;

    private DispensacionService dispensacionService;

    @BeforeEach
    void setUp() {
        dispensacionService = new DispensacionService(
                null, null, null, null, null, null, configuracionService, null, null, null);
    }

    @Test
    void loteUnicoProximoAVencerNoRequiereAutorizacion() {
        LocalDate hoy = LocalDate.now();
        Lote lote = loteQueVenceEn(hoy, 20);

        assertThat(dispensacionService.requiereAutorizacionFefo(List.of(lote), lote, hoy)).isFalse();
    }

    @Test
    void loteProximoAVencerConVariosLotesRequiereAutorizacion() {
        LocalDate hoy = LocalDate.now();
        Lote lote = loteQueVenceEn(hoy, 60);
        when(configuracionService.getDiasProximo()).thenReturn(60);

        assertThat(dispensacionService.requiereAutorizacionFefo(
                List.of(lote, loteQueVenceEn(hoy, 90)), lote, hoy)).isTrue();
    }

    @Test
    void loteFueraDelUmbralNoRequiereAutorizacion() {
        LocalDate hoy = LocalDate.now();
        Lote lote = loteQueVenceEn(hoy, 61);
        when(configuracionService.getDiasProximo()).thenReturn(60);

        assertThat(dispensacionService.requiereAutorizacionFefo(
                List.of(lote, loteQueVenceEn(hoy, 90)), lote, hoy)).isFalse();
    }

    private Lote loteQueVenceEn(LocalDate hoy, long dias) {
        return Lote.builder()
                .fechaVencimiento(hoy.plusDays(dias))
                .build();
    }
}
