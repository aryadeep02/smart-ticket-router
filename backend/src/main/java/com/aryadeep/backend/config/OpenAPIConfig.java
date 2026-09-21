package com.aryadeep.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Configuration
public class OpenAPIConfig {

    @Bean
    public OpenAPI smartTicketRouterOpenAPI() {

        return new OpenAPI()
                .info(
                        new Info()
                                .title("Smart Ticket Router API")
                                .version("1.0.0")
                                .description(
                                        "AI-assisted customer support ticketing platform "
                                                + "built with Spring Boot, PostgreSQL and FastAPI."))
                .components(
                        new Components()
                                .addSecuritySchemes(
                                        "bearerAuth",
                                        new SecurityScheme()
                                                .type(SecurityScheme.Type.HTTP)
                                                .scheme("bearer")
                                                .bearerFormat("JWT")));
    }
}