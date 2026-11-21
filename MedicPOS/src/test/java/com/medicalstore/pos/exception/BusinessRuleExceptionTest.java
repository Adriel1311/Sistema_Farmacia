package com.medicalstore.pos.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BusinessRuleExceptionTest {

    @Test
    void constructor_debeGuardarRuleCodeYMessage() {
        // When
        BusinessRuleException ex = BusinessRuleException.of("TEST_RULE", "Mensaje de prueba");

        // Then
        assertThat(ex.getRuleCode()).isEqualTo("TEST_RULE");
        assertThat(ex.getMessage()).isEqualTo("Mensaje de prueba");
    }

    @Test
    void of_debeCrearInstanciaCorrectamente() {
        // When
        BusinessRuleException ex = BusinessRuleException.of("CODIGO", "Descripción");

        // Then
        assertThat(ex).isInstanceOf(RuntimeException.class);
        assertThat(ex.getRuleCode()).isEqualTo("CODIGO");
    }
}