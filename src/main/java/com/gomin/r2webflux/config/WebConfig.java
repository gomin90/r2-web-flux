package com.gomin.r2webflux.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import java.net.URI;

@Configuration
public class WebConfig implements WebFluxConfigurer {
    
    @Bean
    public RouterFunction<ServerResponse> routerFunction() {
        return RouterFunctions.route()
            .GET("/", request -> 
                ServerResponse.permanentRedirect(URI.create("/swagger-ui.html")).build())
            .build();
    }
}
