package com.medicalstore.pos.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class StockInsufficientExceptionTest {

    @Test
    void constructor_debeGuardarDatosYFormatearMensaje() {
        // When
        StockInsufficientException ex = new StockInsufficientException("Paracetamol", 100, 50);

        // Then
        assertThat(ex.getMessage()).isEqualTo("Stock insuficiente de Paracetamol: se solicitan 100 y solo hay 50 vigentes");
        assertThat(ex.getRequested()).isEqualTo(100);
        assertThat(ex.getAvailable()).isEqualTo(50);
        assertThat(ex.getRuleCode()).isEqualTo("STOCK_INSUFICIENTE");
    }
}