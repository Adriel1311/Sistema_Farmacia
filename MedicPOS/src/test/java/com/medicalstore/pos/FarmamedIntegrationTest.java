package com.medicalstore.pos;

import org.springframework.test.context.ActiveProfiles;
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
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas de integracion contra la base farmamed real.
 *
 * Se ejecutan con la misma configuracion de la aplicacion: esquema farmamed,
 * ddl-auto validate y el usuario de aplicacion farmamed_app. Comprueban los
 * cuatro roles, la matriz de permisos por endpoint, el panel de control y la
 * dispensacion por FEFO con el ticket generado por la base de datos.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class FarmamedIntegrationTest {

    private static final String PASSWORD = "FarmaMed2026";

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate rest;

    @Autowired
    private DispensacionService dispensacionService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private LoteRepository loteRepository;

    private String url(String path) {
        return "http://127.0.0.1:" + port + path;
    }

    private Map<String, Object> loginBody(String usuario, String password) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("usuario", usuario);
        body.put("password", password);
        return body;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> login(String usuario) {
        ResponseEntity<Map> response = rest.postForEntity(
                url("/api/auth/login"), loginBody(usuario, PASSWORD), Map.class);
        assertEquals(HttpStatus.OK, response.getStatusCode(), "Login de " + usuario);
        Map<String, Object> body = response.getBody();
        assertNotNull(body, "Respuesta de login vacia para " + usuario);
        return body;
    }

    private String tokenDe(String usuario) {
        Object token = login(usuario).get("token");
        assertNotNull(token, "No se genero token para " + usuario);
        return token.toString();
    }

    private HttpHeaders conToken(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);
        return headers;
    }

    @Test
    @DisplayName("Los cuatro roles inician sesion y reciben su rol")
    void los_cuatro_roles_inician_sesion() {
        Map<String, String> esperado = Map.of(
                "jefe", "JEFE_FARMACIA",
                "tecnico", "TECNICO_FARMACIA",
                "logistica", "LOGISTICA",
                "admin", "ADMIN_TI");

        esperado.forEach((usuario, rol) -> {
            Map<String, Object> body = login(usuario);
            assertNotNull(body.get("token"), "Falta token de " + usuario);
            assertEquals(rol, body.get("role"), "Rol incorrecto de " + usuario);
        });
    }

    @Test
    @DisplayName("Una contrasena incorrecta se rechaza con 401")
    void contrasena_incorrecta_da_401() {
        RestTemplate simple = new RestTemplate();
        HttpClientErrorException error = assertThrows(HttpClientErrorException.class,
                () -> simple.postForEntity(
                        url("/api/auth/login"), loginBody("jefe", "clave-equivocada"), String.class));
        assertEquals(HttpStatus.UNAUTHORIZED, error.getStatusCode());
    }

    @Test
    @DisplayName("El panel de control responde a los cuatro roles")
    void el_panel_de_control_responde() {
        for (String usuario : List.of("jefe", "tecnico", "logistica", "admin")) {
            ResponseEntity<Map> response = rest.exchange(
                    url("/api/dashboard"), HttpMethod.GET, new HttpEntity<>(conToken(tokenDe(usuario))), Map.class);
            assertEquals(HttpStatus.OK, response.getStatusCode(), "Dashboard de " + usuario);
            assertNotNull(response.getBody(), "Dashboard vacio para " + usuario);
        }
    }

    @Test
    @DisplayName("Logistica no puede dispensar, solo el Jefe y el Tecnico")
    void logistica_no_puede_dispensar() {
        ResponseEntity<Map> response = rest.exchange(
                url("/api/dispensaciones"), HttpMethod.POST,
                new HttpEntity<>(loginBody("logistica", PASSWORD), conToken(tokenDe("logistica"))),
                Map.class);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    @DisplayName("Solo el Jefe de Farmacia accede a la configuracion del sistema")
    void la_configuracion_es_exclusiva_del_jefe() {
        ResponseEntity<String> delTecnico = rest.exchange(
                url("/api/configuracion"), HttpMethod.GET,
                new HttpEntity<>(conToken(tokenDe("tecnico"))), String.class);
        assertEquals(HttpStatus.FORBIDDEN, delTecnico.getStatusCode());

        ResponseEntity<String> delJefe = rest.exchange(
                url("/api/configuracion"), HttpMethod.GET,
                new HttpEntity<>(conToken(tokenDe("jefe"))), String.class);
        assertEquals(HttpStatus.OK, delJefe.getStatusCode());
        assertNotNull(delJefe.getBody());
    }

    @Test
    @Transactional
    @DisplayName("La dispensacion toma el lote FEFO y el ticket lo genera la base de datos")
    void la_dispensacion_usa_fefo_y_el_ticket_de_la_base_de_datos() {
        CreateDispensacionRequest request = new CreateDispensacionRequest();
        request.setRecetaId(2L);

        DispensacionItemRequest item = new DispensacionItemRequest();
        item.setMedicamentoId(1L);
        item.setCantidad(5);
        request.setItems(List.of(item));

        Usuario tecnico = usuarioRepository.findByUsuarioWithRol("tecnico")
                .orElseThrow(() -> new IllegalStateException("El usuario tecnico no existe"));

        DispensacionResponse response = dispensacionService.create(request, tecnico, new MockHttpServletRequest());

        assertNotNull(response.getNumeroTicket(), "La dispensacion no tiene ticket");
        assertTrue(response.getNumeroTicket().matches("^TI-\\d{6}$"),
                "Formato de ticket inesperado: " + response.getNumeroTicket());

        assertEquals(1, response.getDetalles().size());
        assertEquals(5, response.getDetalles().get(0).getCantidad());

        List<Lote> candidatos = loteRepository.findByMedicamentoIdWithMedicamento(1L).stream()
                .filter(l -> l.getEstado() == Lote.Estado.ACTIVO)
                .filter(l -> l.getCantidadActual() > 0)
                .filter(l -> !l.getFechaVencimiento().isBefore(LocalDate.now()))
                .sorted(Comparator.comparing(Lote::getFechaVencimiento))
                .toList();

        assertFalse(candidatos.isEmpty(), "No hay lotes vigentes de prueba");
        assertEquals(candidatos.get(0).getId(), response.getDetalles().get(0).getLoteId(),
                "La dispensacion no tomo el lote que vence primero");
    }
}
