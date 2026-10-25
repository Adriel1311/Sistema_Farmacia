package com.medicalstore.pos;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas de la exportacion de reportes (RF-12) y de las etiquetas de codigo
 * de barras y QR contra la base farmamed real.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ReporteExportIntegrationTest {
    
    private static final String PASSWORD = "FarmaMed2026";
    private static final byte[] FIRMA_PDF = {'%', 'P', 'D', 'F'};
    private static final byte[] FIRMA_ZIP = {'P', 'K', 0x03, 0x04};
    private static final byte[] FIRMA_PNG = {(byte) 0x89, 'P', 'N', 'G'};
    
    @LocalServerPort
    private int port;
    
    @Autowired
    private TestRestTemplate rest;
    
    private String url(String path) {
        return "http://127.0.0.1:" + port + path;
    }
    
    private String tokenDe(String usuario) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("usuario", usuario);
        body.put("password", PASSWORD);
        ResponseEntity<Map> response = rest.postForEntity(url("/api/auth/login"), body, Map.class);
        assertEquals(HttpStatus.OK, response.getStatusCode(), "Login de " + usuario);
        Object token = response.getBody().get("token");
        assertNotNull(token, "No se genero token para " + usuario);
        return token.toString();
    }
    
    private HttpHeaders conToken(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        return headers;
    }
    
    private ResponseEntity<byte[]> descargar(String ruta, String token) {
        return rest.exchange(url(ruta), HttpMethod.GET, new HttpEntity<>(conToken(token)), byte[].class);
    }
    
    @Test
    @DisplayName("El inventario se exporta a PDF y a XLSX")
    void el_inventario_se_exporta_en_pdf_y_xlsx() {
        String token = tokenDe("jefe");
        
        ResponseEntity<byte[]> pdf = descargar("/api/reportes/inventario/pdf", token);
        assertEquals(HttpStatus.OK, pdf.getStatusCode());
        assertTrue(String.valueOf(pdf.getHeaders().getContentType()).startsWith("application/pdf"),
                "El PDF no se sirve como application/pdf");
        assertArrayEquals(FIRMA_PDF, primeros(pdf.getBody(), FIRMA_PDF.length), "El PDF no es valido");
        
        ResponseEntity<byte[]> xlsx = descargar("/api/reportes/inventario/xlsx", token);
        assertEquals(HttpStatus.OK, xlsx.getStatusCode());
        assertTrue(String.valueOf(xlsx.getHeaders().getContentType())
                .contains("spreadsheetml"), "El XLSX no se sirve como hoja de calculo");
        assertArrayEquals(FIRMA_ZIP, primeros(xlsx.getBody(), FIRMA_ZIP.length), "El XLSX no es valido");
        
        String disposicion = String.valueOf(pdf.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION));
        assertTrue(disposicion.contains("attachment") && disposicion.contains(".pdf"),
                "El PDF no se descarga como adjunto: " + disposicion);
    }
    
    @Test
    @DisplayName("Los cuatro reportes del modulo se exportan a PDF")
    void los_cuatro_reportes_se_exportan_a_pdf() {
        String token = tokenDe("jefe");
        for (String tipo : new String[] {"consumo", "inventario", "bajas", "ordenes"}) {
            ResponseEntity<byte[]> pdf = descargar("/api/reportes/" + tipo + "/pdf", token);
            assertEquals(HttpStatus.OK, pdf.getStatusCode(), "PDF de " + tipo + " -> " + new String(pdf.getBody(), StandardCharsets.UTF_8));
            assertArrayEquals(FIRMA_PDF, primeros(pdf.getBody(), FIRMA_PDF.length), "PDF de " + tipo);
        }
    }
    
    @Test
    @DisplayName("Un tipo de reporte desconocido se rechaza")
    void un_tipo_desconocido_se_rechaza() {
        String token = tokenDe("jefe");
        ResponseEntity<byte[]> response = descargar("/api/reportes/inventario-xyz/pdf", token);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
    
    @Test
    @DisplayName("El Tecnico no puede exportar reportes")
    void el_tecnico_no_exporta_reportes() {
        String token = tokenDe("tecnico");
        ResponseEntity<byte[]> response = descargar("/api/reportes/inventario/pdf", token);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }
    
    @Test
    @DisplayName("Se generan las etiquetas de codigo de barras y el QR del comprobante")
    void se_generan_las_etiquetas() {
        String token = tokenDe("jefe");
        
        ResponseEntity<byte[]> medicamento = descargar("/api/codigos-barras/medicamento/1", token);
        assertEquals(HttpStatus.OK, medicamento.getStatusCode());
        assertEquals(MediaType.IMAGE_PNG, medicamento.getHeaders().getContentType());
        assertArrayEquals(FIRMA_PNG, primeros(medicamento.getBody(), FIRMA_PNG.length));
        
        ResponseEntity<byte[]> lote = descargar("/api/codigos-barras/lote/1", token);
        assertEquals(HttpStatus.OK, lote.getStatusCode());
        assertArrayEquals(FIRMA_PNG, primeros(lote.getBody(), FIRMA_PNG.length));
        
        ResponseEntity<byte[]> comprobante = descargar("/api/codigos-barras/dispensacion/1", token);
        assertEquals(HttpStatus.OK, comprobante.getStatusCode());
        assertArrayEquals(FIRMA_PNG, primeros(comprobante.getBody(), FIRMA_PNG.length));
    }
    
    private byte[] primeros(byte[] contenido, int cantidad) {
        assertNotNull(contenido, "El archivo descargado esta vacio");
        byte[] inicio = new byte[cantidad];
        System.arraycopy(contenido, 0, inicio, 0, cantidad);
        return inicio;
    }
}
