package com.medicalstore.pos.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ResourceNotFoundExceptionTest {

    @Test
    void constructorConMensaje_debeGuardarMensaje() {
        // When
        ResourceNotFoundException ex = new ResourceNotFoundException("Recurso no encontrado");

        // Then
        assertThat(ex.getMessage()).isEqualTo("Recurso no encontrado");
    }

    @Test
    void constructorConDetalles_debeFormatearMensaje() {
        // When
        ResourceNotFoundException ex = new ResourceNotFoundException("Usuario", "email", "test@test.com");

        // Then
        assertThat(ex.getMessage()).isEqualTo("Usuario not found with email: test@test.com");
    }
}