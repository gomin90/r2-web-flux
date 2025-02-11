package com.gomin.r2webflux.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
    
    @Bean
    public OpenAPI openAPI() {
        Info info = new Info()
                .title("R2DBC Account API")
                .description("Reactive Account Management API Documentation")
                .version("v1.0")
                .contact(new Contact()
                        .name("Gomin")
                        .email("gomin@icanman.co.kr"));

        // SecuritySchemes 설정 추가
        Components components = new Components()
                .addSecuritySchemes("bearer-jwt", new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT"))
                .addSecuritySchemes("oauth2", new SecurityScheme()
                        .type(SecurityScheme.Type.OAUTH2)
                        .scheme("oauth2"));

        return new OpenAPI()
                .info(info)
                .components(components);
    }
    
    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .group("public-api")
                .pathsToMatch("/api/**", "/auth/**")  // auth 경로 추가
                .build();
    }
}
