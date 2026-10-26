package com.medicalstore.pos.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.medicalstore.pos.dto.response.DniData;
import com.medicalstore.pos.dto.response.DniResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class FactilizaClient {
    private final WebClient webClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public FactilizaClient(WebClient.Builder builder,
                           @org.springframework.beans.factory.annotation.Value("${factiliza.base-url}") String baseUrl,
                           @org.springframework.beans.factory.annotation.Value("${factiliza.token}") String token,
                           @org.springframework.beans.factory.annotation.Value("${factiliza.timeout-ms:5000}") int timeoutMs,
                           @org.springframework.beans.factory.annotation.Value("${factiliza.connect-timeout-ms:3000}") int connectTimeoutMs) {
        this.webClient = builder
                .baseUrl(baseUrl)
                .defaultHeader("Authorization", "Bearer " + token)
                .defaultHeader("Accept", "application/json")
                .build();
    }

    public Mono<DniResponse> consultarDni(String dni) {
        return webClient.get()
                .uri("/v1/dni/info/{dni}", dni)
                .retrieve()
                .onStatus(status -> status.is4xxClientError(),
                        response -> response.bodyToMono(String.class)
                                .flatMap(body -> {
                                    try {
                                        JsonNode errorNode = objectMapper.readTree(body);
                                        String message = errorNode.path("message").asText("Error en Factiliza");
                                        return Mono.error(new FactilizaException(message, response.statusCode().value()));
                                    } catch (JsonProcessingException e) {
                                        return Mono.error(new FactilizaException("Error parseando error de Factiliza", response.statusCode().value()));
                                    } catch (Exception e) {
                                        return Mono.error(new FactilizaException("Error en Factiliza: " + response.statusCode().value(), response.statusCode().value()));
                                    }
                                }))
                .onStatus(status -> status.is5xxServerError(),
                        response -> Mono.error(new FactilizaException("Error en servicio Factiliza", response.statusCode().value())))
                .bodyToMono(String.class)
                .timeout(java.time.Duration.ofMillis(5000))
                .map(this::parseResponse)
                .onErrorMap(e -> e instanceof FactilizaException ? e : new FactilizaException("Timeout o error de conexión con Factiliza", 504));
    }

    private DniResponse parseResponse(String responseBody) {
        try {
            JsonNode rootNode = objectMapper.readTree(responseBody);

            // Check if response indicates success
            Boolean success = rootNode.path("success").asBoolean(false);
            Integer status = rootNode.path("status").asInt(0);

            if (!success || status != 200) {
                String message = rootNode.path("message").asText("Error en respuesta de Factiliza");
                throw new FactilizaException(message, status);
            }

            // Parse the nested data object
            JsonNode dataNode = rootNode.path("data");
            if (dataNode.isMissingNode() || dataNode.isNull()) {
                throw new FactilizaException("Respuesta de Factiliza sin datos", 502);
            }

            // Create DniResponse and populate from nested data
            DniResponse response = new DniResponse();
            response.setStatus(status);
            response.setMessage(rootNode.path("message").asText());
            response.setSuccess(success);
            response.setFuente(rootNode.path("fuente").asInt(0));

            // Create and populate DniData object
            DniData data = new DniData();
            data.setNumero(dataNode.path("numero").asText(""));
            data.setCodigoVerificacion(dataNode.path("codigo_verificacion").asText(""));
            data.setNombres(dataNode.path("nombres").asText(""));
            data.setApellidoPaterno(dataNode.path("apellido_paterno").asText(""));
            data.setApellidoMaterno(dataNode.path("apellido_materno").asText(""));
            data.setNombreCompleto(dataNode.path("nombre_completo").asText(""));
            data.setDepartamento(dataNode.path("departamento").asText(""));
            data.setProvincia(dataNode.path("provincia").asText(""));
            data.setDistrito(dataNode.path("distrito").asText(""));
            data.setDireccion(dataNode.path("direccion").asText(""));
            data.setDireccionCompleta(dataNode.path("direccion_completa").asText(""));
            data.setUbigeoReniec(dataNode.path("ubigeo_reniec").asText(""));
            data.setUbigeoSunat(dataNode.path("ubigeo_sunat").asText(""));
            data.setFechaNacimiento(dataNode.path("fecha_nacimiento").asText(""));
            data.setEstadoCivil(dataNode.path("estado_civil").asText(""));
            data.setFoto(dataNode.path("foto").asText(""));
            data.setSexo(dataNode.path("sexo").asText(""));

            // Parse ubigeo array
            if (dataNode.has("ubigeo") && dataNode.get("ubigeo").isArray()) {
                data.setUbigeo(objectMapper.convertValue(dataNode.get("ubigeo"),
                        objectMapper.getTypeFactory().constructCollectionType(java.util.List.class, String.class)));
            }

            response.setData(data);

            // Also set flat fields for backward compatibility
            response.setNombres(data.getNombres());
            String apellidos = "";
            if (data.getApellidoPaterno() != null) {
                apellidos = data.getApellidoPaterno();
            }
            if (data.getApellidoMaterno() != null) {
                if (!apellidos.isEmpty()) apellidos += " ";
                apellidos += data.getApellidoMaterno();
            }
            response.setApellidos(apellidos);
            response.setDni(data.getNumero());
            response.setNombreCompleto(data.getNombreCompleto());
            response.setDireccion(data.getDireccionCompleta() != null ? data.getDireccionCompleta() : data.getDireccion());

            return response;

        } catch (FactilizaException e) {
            throw e;
        } catch (Exception e) {
            throw new FactilizaException("Error parseando respuesta de Factiliza: " + e.getMessage(), 502);
        }
    }

    public static class FactilizaException extends RuntimeException {
        private final int statusCode;

        public FactilizaException(String message, int statusCode) {
            super(message);
            this.statusCode = statusCode;
        }

        public int getStatusCode() {
            return statusCode;
        }
    }
}