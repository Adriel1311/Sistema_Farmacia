package com.medicalstore.pos.entity;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class MedicamentoTest {

    @Test
    void getCantidadSugerida_debeRetornarDiferenciaEntreMaximoYStockVigente() {
        // Given
        Medicamento medicamento = Medicamento.builder()
                .stockMinimo(20)
                .stockMaximo(200)
                .build();

        // When
        int sugerida = medicamento.getCantidadSugerida(50);

        // Then
        assertThat(sugerida).isEqualTo(150); // 200 - 50
    }

    @Test
    void getCantidadSugerida_debeRetornarCeroSiStockSuperaMaximo() {
        // Given
        Medicamento medicamento = Medicamento.builder()
                .stockMinimo(20)
                .stockMaximo(200)
                .build();

        // When
        int sugerida = medicamento.getCantidadSugerida(250);

        // Then
        assertThat(sugerida).isEqualTo(0);
    }

    @Test
    void getCantidadSugerida_debeRetornarMaximoSiStockEsCero() {
        // Given
        Medicamento medicamento = Medicamento.builder()
                .stockMinimo(20)
                .stockMaximo(200)
                .build();

        // When
        int sugerida = medicamento.getCantidadSugerida(0);

        // Then
        assertThat(sugerida).isEqualTo(200);
    }

    @ParameterizedTest
    @CsvSource({
        "10, 5, true",   // stock < minimo
        "10, 10, true",  // stock == minimo
        "10, 15, false", // stock > minimo
        "0, 0, true"     // stock == minimo == 0
    })
    void isStockCritico_debeDetectarCorrectamente(int stockMinimo, int stockVigente, boolean esperadoCritico) {
        // Given
        Medicamento medicamento = Medicamento.builder()
                .stockMinimo(stockMinimo)
                .stockMaximo(stockMinimo + 100)
                .build();

        // When
        boolean critico = medicamento.isStockCritico(stockVigente);

        // Then
        assertThat(critico).isEqualTo(esperadoCritico);
    }

    @Test
    void costoUnitario_debeSerCeroPorDefecto() {
        // Given
        Medicamento medicamento = Medicamento.builder().build();

        // When
        BigDecimal costo = medicamento.getCostoUnitario();

        // Then
        assertThat(costo).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void costoUnitario_debePermitirValorPersonalizado() {
        // Given
        Medicamento medicamento = Medicamento.builder()
                .costoUnitario(new BigDecimal("12.50"))
                .build();

        // When
        BigDecimal costo = medicamento.getCostoUnitario();

        // Then
        assertThat(costo).isEqualByComparingTo(new BigDecimal("12.50"));
    }
}