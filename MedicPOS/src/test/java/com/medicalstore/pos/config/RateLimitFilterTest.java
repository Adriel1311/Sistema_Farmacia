package com.medicalstore.pos.config;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RateLimitFilterTest {

    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private FilterChain filterChain;

    private RateLimitFilter filter;
    private StringWriter responseWriter;

    @BeforeEach
    void setUp() throws IOException {
        filter = new RateLimitFilter(10, 5); // 10 req/min general, 5 req/min login
        responseWriter = new StringWriter();
        lenient().when(response.getWriter()).thenReturn(new PrintWriter(responseWriter));
    }

    @AfterEach
    void tearDown() {
        // Clean up any buckets created during tests
    }

    @Test
    void doFilterInternal_debePermitirRequestsDentroDelLimite() throws ServletException, IOException {
        // Given
        when(request.getRequestURI()).thenReturn("/api/test");
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");

        // When - hacer 5 requests (dentro del límite de 10)
        for (int i = 0; i < 5; i++) {
            filter.doFilterInternal(request, response, filterChain);
        }

        // Then
        verify(filterChain, times(5)).doFilter(request, response);
        assertThat(responseWriter.toString()).isEmpty();
    }

    @Test
    void doFilterInternal_debeBloquearRequestsQueExcedenLimite() throws ServletException, IOException {
        // Given
        when(request.getRequestURI()).thenReturn("/api/test");
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");

        // When - hacer 11 requests (límite es 10)
        for (int i = 0; i < 11; i++) {
            filter.doFilterInternal(request, response, filterChain);
        }

        // Then
        verify(filterChain, times(10)).doFilter(request, response);
        // El 11avo debe ser bloqueado
        assertThat(responseWriter.toString()).contains("Too Many Requests");
        assertThat(responseWriter.toString()).contains("Rate limit exceeded");
    }

    @Test
    void doFilterInternal_debeAplicarLimiteMasEstrictoParaLogin() throws ServletException, IOException {
        // Given
        when(request.getRequestURI()).thenReturn("/api/auth/login");
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");

        // When - hacer 6 requests a login (límite es 5)
        for (int i = 0; i < 6; i++) {
            filter.doFilterInternal(request, response, filterChain);
        }

        // Then
        verify(filterChain, times(5)).doFilter(request, response);
        assertThat(responseWriter.toString()).contains("Too Many Requests");
    }

    @Test
    void doFilterInternal_debeAplicarLimiteDNI() throws ServletException, IOException {
        // Given
        when(request.getRequestURI()).thenReturn("/api/dni/12345678");
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");

        // When - hacer 31 requests a DNI (límite es 30)
        for (int i = 0; i < 31; i++) {
            filter.doFilterInternal(request, response, filterChain);
        }

        // Then
        verify(filterChain, times(30)).doFilter(request, response);
        assertThat(responseWriter.toString()).contains("Too Many Requests");
    }

    @Test
    void getClientIp_debeUsarXForwardedForCuandoEstaPresente() throws ServletException, IOException {
        // Given
        lenient().when(request.getHeader("X-Forwarded-For")).thenReturn("192.168.1.1, 10.0.0.1");
        lenient().when(request.getRequestURI()).thenReturn("/api/test");
        lenient().when(request.getRemoteAddr()).thenReturn("127.0.0.1");

        // When
        filter.doFilterInternal(request, response, filterChain);

        // Then - la IP usada para el bucket debería ser la primera del X-Forwarded-For
        // Verificamos indirectamente que no falla
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void getClientIp_debeUsarXRealIPCuandoXForwardedForNoEsta() throws ServletException, IOException {
        // Given
        lenient().when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        lenient().when(request.getHeader("X-Real-IP")).thenReturn("192.168.1.50");
        lenient().when(request.getRequestURI()).thenReturn("/api/test");
        lenient().when(request.getRemoteAddr()).thenReturn("127.0.0.1");

        // When
        filter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void getClientIp_debeUsarRemoteAddrComoFallback() throws ServletException, IOException {
        // Given
        lenient().when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        lenient().when(request.getHeader("X-Real-IP")).thenReturn(null);
        lenient().when(request.getRequestURI()).thenReturn("/api/test");
        lenient().when(request.getRemoteAddr()).thenReturn("127.0.0.1");

        // When
        filter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
    }
}