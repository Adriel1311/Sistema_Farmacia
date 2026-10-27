package com.medicalstore.pos.service;

import com.medicalstore.pos.entity.Lote;
import com.medicalstore.pos.repository.LoteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StockServiceTest {

    @Mock
    private LoteRepository loteRepository;

    private StockService stockService;

    @BeforeEach
    void setUp() {
        stockService = new StockService(loteRepository);
    }

    @Test
    void getStockVigente_debeRetornarSumaDeLotesVigentes() {
        // Given
        Long medicamentoId = 1L;
        LocalDate hoy = LocalDate.now();
        when(loteRepository.getStockVigente(medicamentoId, hoy)).thenReturn(50);

        // When
        int stock = stockService.getStockVigente(medicamentoId, hoy);

        // Then
        assertThat(stock).isEqualTo(50);
    }

    @Test
    void getStockVigente_debeRetornarCeroCuandoNoHayLotes() {
        // Given
        Long medicamentoId = 1L;
        LocalDate hoy = LocalDate.now();
        when(loteRepository.getStockVigente(medicamentoId, hoy)).thenReturn(null);

        // When
        int stock = stockService.getStockVigente(medicamentoId, hoy);

        // Then
        assertThat(stock).isEqualTo(0);
    }

    @Test
    void getStockVigentePorIds_debeRetornarMapaConStockPorMedicamento() {
        // Given
        LocalDate hoy = LocalDate.now();
        List<Long> ids = List.of(1L, 2L, 3L);
        when(loteRepository.getStockVigentePorIds(ids, hoy)).thenReturn(List.of(
            new Object[]{1L, 25},
            new Object[]{2L, 30},
            new Object[]{3L, 0}
        ));

        // When
        var result = stockService.getStockVigentePorIds(ids, hoy);

        // Then
        assertThat(result).hasSize(3);
        assertThat(result.get(1L)).isEqualTo(25);
        assertThat(result.get(2L)).isEqualTo(30);
        assertThat(result.get(3L)).isEqualTo(0);
    }

    @Test
    void getStockVigentePorIds_debeRetornarMapaVacioCuandoListaVacia() {
        // When
        var result = stockService.getStockVigentePorIds(Collections.emptyList(), LocalDate.now());

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void getStockVencido_debeRetornarStockDeLotesVencidos() {
        // Given
        Long medicamentoId = 1L;
        LocalDate hoy = LocalDate.now();
        when(loteRepository.getStockVencido(medicamentoId, hoy)).thenReturn(15);

        // When
        int stock = stockService.getStockVencido(medicamentoId, hoy);

        // Then
        assertThat(stock).isEqualTo(15);
    }

    @Test
    void countLotesActivos_debeRetornarConteoDeLotesConStock() {
        // Given
        Long medicamentoId = 1L;
        when(loteRepository.countByMedicamentoIdAndCantidadActualGreaterThan(medicamentoId, 0)).thenReturn(3L);

        // When
        long count = stockService.countLotesActivos(medicamentoId);

        // Then
        assertThat(count).isEqualTo(3L);
    }

    @Test
    void countLotesVencidos_debeRetornarConteoDeLotesVencidosConStock() {
        // Given
        Long medicamentoId = 1L;
        LocalDate hoy = LocalDate.now();
        when(loteRepository.countLotesVencidos(medicamentoId, hoy)).thenReturn(2L);

        // When
        long count = stockService.countLotesVencidos(medicamentoId, hoy);

        // Then
        assertThat(count).isEqualTo(2L);
    }
}