package com.gomin.r2webflux.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;

@Configuration
public class SwaggerConfig {
    
    @Value("${springdoc.swagger-ui.theme:newspaper}")
    private String theme;
    
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("R2DBC Account API")
                        .description("""
                            Reactive Account Management API Documentation
                            
                            Theme: %s
                            """.formatted(theme))
                        .version("v1.0")
                        .contact(new Contact()
                                .name("Gomin")
                                .email("gomin@icanman.co.kr")));
    }
    
    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .group("public-api")
                .pathsToMatch("/api/**")
                .build();
    }
}
