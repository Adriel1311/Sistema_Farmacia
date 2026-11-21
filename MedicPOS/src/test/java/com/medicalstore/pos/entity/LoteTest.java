package com.medicalstore.pos.entity;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class LoteTest {

    @Test
    void getDiasRestantes_debeCalcularDiferenciaCorrecta() {
        // Given
        LocalDate mañana = LocalDate.now().plusDays(1);
        Lote lote = Lote.builder()
                .fechaVencimiento(mañana)
                .build();

        // When
        long dias = lote.getDiasRestantes();

        // Then
        assertThat(dias).isEqualTo(1);
    }

    @Test
    void getDiasRestantes_debeSerNegativoSiYaVencio() {
        // Given
        LocalDate ayer = LocalDate.now().minusDays(1);
        Lote lote = Lote.builder()
                .fechaVencimiento(ayer)
                .build();

        // When
        long dias = lote.getDiasRestantes();

        // Then
        assertThat(dias).isEqualTo(-1);
    }

    @Test
    void isVencido_debeSerTrueSiFechaVencimientoEsPasado() {
        // Given
        Lote lote = Lote.builder()
                .fechaVencimiento(LocalDate.now().minusDays(5))
                .build();

        // When
        boolean vencido = lote.isVencido();

        // Then
        assertThat(vencido).isTrue();
    }

    @Test
    void isVencido_debeSerFalseSiFechaVencimientoEsFuturo() {
        // Given
        Lote lote = Lote.builder()
                .fechaVencimiento(LocalDate.now().plusDays(5))
                .build();

        // When
        boolean vencido = lote.isVencido();

        // Then
        assertThat(vencido).isFalse();
    }

    @Test
    void isVigente_debeSerTrueSoloSiActivoConStockYNoVencido() {
        // Given
        Lote lote = Lote.builder()
                .estado(Lote.Estado.ACTIVO)
                .cantidadActual(10)
                .fechaVencimiento(LocalDate.now().plusDays(10))
                .build();

        // When
        boolean vigente = lote.isVigente();

        // Then
        assertThat(vigente).isTrue();
    }

    @Test
    void isVigente_debeSerFalseSiEstadoEsBaja() {
        // Given
        Lote lote = Lote.builder()
                .estado(Lote.Estado.BAJA)
                .cantidadActual(10)
                .fechaVencimiento(LocalDate.now().plusDays(10))
                .build();

        // When
        boolean vigente = lote.isVigente();

        // Then
        assertThat(vigente).isFalse();
    }

    @Test
    void isVigente_debeSerFalseSiStockEsCero() {
        // Given
        Lote lote = Lote.builder()
                .estado(Lote.Estado.ACTIVO)
                .cantidadActual(0)
                .fechaVencimiento(LocalDate.now().plusDays(10))
                .build();

        // When
        boolean vigente = lote.isVigente();

        // Then
        assertThat(vigente).isFalse();
    }

    @Test
    void isVigente_debeSerFalseSiVencido() {
        // Given
        Lote lote = Lote.builder()
                .estado(Lote.Estado.ACTIVO)
                .cantidadActual(10)
                .fechaVencimiento(LocalDate.now().minusDays(1))
                .build();

        // When
        boolean vigente = lote.isVigente();

        // Then
        assertThat(vigente).isFalse();
    }

    @ParameterizedTest
    @CsvSource({
        "BAJA, -1, NEGRO",
        "ACTIVO, -5, NEGRO",
        "ACTIVO, 0, ROJO",
        "ACTIVO, 15, ROJO",
        "ACTIVO, 30, ROJO",
        "ACTIVO, 31, AMBAR",
        "ACTIVO, 45, AMBAR",
        "ACTIVO, 60, AMBAR",
        "ACTIVO, 61, VERDE",
        "ACTIVO, 100, VERDE"
    })
    void getEstadoVencimiento_debeRetornarColorCorrectoSegunDiasRestantes(String estado, long diasRestantes, String esperado) {
        // Given
        LocalDate fechaVencimiento = LocalDate.now().plusDays(diasRestantes);
        Lote lote = Lote.builder()
                .estado(Lote.Estado.valueOf(estado))
                .fechaVencimiento(fechaVencimiento)
                .build();

        // When
        Lote.EstadoVencimiento resultado = lote.getEstadoVencimiento(30, 60);

        // Then
        assertThat(resultado.name()).isEqualTo(esperado);
    }

    @Test
    void tieneStock_debeSerTrueSiCantidadActualEsSuficiente() {
        // Given
        Lote lote = Lote.builder()
                .cantidadActual(50)
                .build();

        // When
        boolean tiene = lote.tieneStock(30);

        // Then
        assertThat(tiene).isTrue();
    }

    @Test
    void tieneStock_debeSerFalseSiCantidadActualEsInsuficiente() {
        // Given
        Lote lote = Lote.builder()
                .cantidadActual(10)
                .build();

        // When
        boolean tiene = lote.tieneStock(20);

        // Then
        assertThat(tiene).isFalse();
    }
}