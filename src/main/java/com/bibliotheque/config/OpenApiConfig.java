package com.bibliotheque.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI bibliothequeOpenAPI() {
        return new OpenAPI()
                .info(new Info().title("Bibliothèque Management API")
                        .description("API de gestion de bibliothèque avec Spring Boot")
                        .version("v1.0")
                        .contact(new Contact().name("Admin").email("admin@biblio.com"))
                        .license(new License().name("Apache 2.0").url("http://springdoc.org")));
    }
}
