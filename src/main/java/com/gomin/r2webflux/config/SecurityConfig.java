package com.gomin.r2webflux.config;

import com.gomin.r2webflux.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URI;

@Configuration
@EnableWebFluxSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    
    private final JwtTokenProvider jwtTokenProvider;
    private static final String SWAGGER_UI_PATH = "/swagger-ui.html";

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers("/auth/**", "/login/**").permitAll()
                        .pathMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .pathMatchers(HttpMethod.OPTIONS).permitAll()
                        .anyExchange().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                        .authenticationSuccessHandler((webFilterExchange, authentication) -> {
                            ServerWebExchange exchange = webFilterExchange.getExchange();
                            return jwtTokenProvider.onAuthenticationSuccess(webFilterExchange, authentication)
                                    .then(Mono.fromRunnable(() -> {
                                        exchange.getResponse().getHeaders()
                                                .setLocation(URI.create(SWAGGER_UI_PATH));
                                        exchange.getResponse().setStatusCode(HttpStatus.FOUND);
                                    }));
                        })
                        .authenticationFailureHandler((webFilterExchange, exception) -> {
                            ServerWebExchange exchange = webFilterExchange.getExchange();
                            exchange.getResponse().setStatusCode(HttpStatus.FOUND);
                            exchange.getResponse().getHeaders()
                                    .setLocation(URI.create(SWAGGER_UI_PATH));
                            return Mono.empty();
                        })
                )
                .logout(logout -> logout
                        .logoutUrl("/auth/logout")
                        .logoutSuccessHandler((exchange, authentication) -> 
                            exchange.getExchange().getSession()
                                .flatMap(session -> {
                                    session.invalidate();
                                    return Mono.empty();
                                })
                        )
                )
                .build();
    }
}
