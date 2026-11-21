package com.medicalstore.pos.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ConflictExceptionTest {

    @Test
    void constructorConMensaje_debeGuardarMensajeYRuleCode() {
        // When
        ConflictException ex = new ConflictException("El recurso ya existe");

        // Then
        assertThat(ex.getMessage()).isEqualTo("El recurso ya existe");
        assertThat(ex.getRuleCode()).isEqualTo("CONFLICT");
    }

    @Test
    void constructorConDetalles_debeFormatearMensaje() {
        // When
        ConflictException ex = new ConflictException("Usuario", "email", "test@test.com");

        // Then
        assertThat(ex.getMessage()).isEqualTo("Usuario already exists with email: test@test.com");
        assertThat(ex.getRuleCode()).isEqualTo("CONFLICT");
    }
}