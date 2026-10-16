package com.example.demo_test.configuration;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Configuration
public class OpenApiConfiguration {

    @Bean
    public OpenAPI springOpenAPI() {
        return new OpenAPI()
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .info(new Info()
                        .title("Api Documentation Demo Test")
                        .description("Api Documentation Demo Test")
                        .version("v1.0.0"));
    }

    @Bean
    public GroupedOpenApi apiGroupA() {
        return GroupedOpenApi.builder()
                .group("API A")
                .pathsToMatch("/**")
                .packagesToScan("com.example.demo_test.controller")
                .build();
    }

    @Bean
    public GroupedOpenApi publicApiB() {
        return GroupedOpenApi.builder()
                .group("API B - Management")
                .pathsToMatch("/management/**")
                .packagesToScan("com.example.demo_test.controller")
                .build();
    }
}
