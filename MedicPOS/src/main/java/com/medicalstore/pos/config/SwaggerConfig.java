package com.medicalstore.pos.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
    
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("FarmaMed API")
                        .version("1.0.0")
                        .description("API de gestion de farmacia: medicamentos, lotes, dispensacion FEFO, "
                                + "abastecimiento, alertas y reportes")
                        .contact(new Contact()
                                .name("FarmaMed")
                                .email("soporte@farmamed.pe"))
                        .license(new License()
                                .name("Proyecto academico")
                                .url("https://farmamed.pe")))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("JWT token authentication")));
    }
}







