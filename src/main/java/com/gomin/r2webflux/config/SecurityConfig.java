package com.gomin.r2webflux.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.web.server.DefaultServerRedirectStrategy;
import org.springframework.security.web.server.ServerRedirectStrategy;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import reactor.core.publisher.Mono;
import java.net.URI;

@Configuration
@EnableWebFluxSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    
    private final ServerRedirectStrategy redirectStrategy = new DefaultServerRedirectStrategy();
    
    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .pathMatchers("/actuator/**").permitAll()
                        .pathMatchers("/login/**").permitAll()
                        .anyExchange().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                        .authenticationSuccessHandler((webFilterExchange, authentication) -> {
                            ServerHttpResponse response = webFilterExchange.getExchange().getResponse();
                            response.getHeaders().setLocation(URI.create("https://r2-web-flux-8a261ca95b28.herokuapp.com"));
                            response.setStatusCode(HttpStatus.TEMPORARY_REDIRECT);
                            return Mono.empty();
                        })
                )
                .exceptionHandling(exceptionHandling -> exceptionHandling
                        .authenticationEntryPoint((exchange, ex) -> 
                            redirectStrategy.sendRedirect(exchange, 
                                URI.create("/oauth2/authorization/github"))
                        )
                )
                .build();
    }
}
