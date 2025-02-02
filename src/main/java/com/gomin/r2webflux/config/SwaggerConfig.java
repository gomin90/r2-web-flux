package com.gomin.r2webflux.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.Operation;
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

        return new OpenAPI()
                .info(info)
                .components(new Components());
    }
    
    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .group("public-api")
                .pathsToMatch("/api/**")
                .addOpenApiCustomizer(openApi -> {
                    // Operation level customization
                    openApi.getPaths().values().forEach(pathItem -> {
                        if (pathItem.getGet() != null) {
                            customizeOperation(pathItem.getGet());
                        }
                        if (pathItem.getPost() != null) {
                            customizeOperation(pathItem.getPost());
                        }
                        if (pathItem.getPut() != null) {
                            customizeOperation(pathItem.getPut());
                        }
                        if (pathItem.getDelete() != null) {
                            customizeOperation(pathItem.getDelete());
                        }
                    });
                })
                .build();
    }

    private void customizeOperation(Operation operation) {
        if (operation.getTags() == null) {
            operation.addTagsItem("Account API");
        }
    }
}
