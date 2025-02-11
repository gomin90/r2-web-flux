package com.gomin.r2webflux.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication management API")
@SecurityRequirement(name = "bearer-jwt")  // 컨트롤러 레벨에서 보안 요구사항 추가
public class AuthenticationController {

    @Operation(
        summary = "Get user information",
        description = "Retrieves authenticated user information",
        security = { @SecurityRequirement(name = "bearer-jwt") }
    )
    @GetMapping("/user")
    public Mono<ResponseEntity<Map<String, Object>>> getUser(@AuthenticationPrincipal OAuth2User principal) {
        Map<String, Object> response = new HashMap<>();
        if (principal != null) {
            response.put("name", principal.getName());
            response.put("attributes", principal.getAttributes());
        }
        return Mono.just(new ResponseEntity<>(response, HttpStatus.OK));
    }

    @Operation(
        summary = "Login success callback",
        description = "Handles successful OAuth2 login",
        security = { @SecurityRequirement(name = "oauth2") }
    )
    @GetMapping("/login/success")
    public Mono<ResponseEntity<Map<String, String>>> loginSuccess(@AuthenticationPrincipal OAuth2User principal) {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Login successful");
        response.put("email", principal.getAttribute("email"));
        return Mono.just(new ResponseEntity<>(response, HttpStatus.OK));
    }

    @Operation(
        summary = "Logout",
        description = "Logs out the current user",
        security = { @SecurityRequirement(name = "bearer-jwt") }
    )
    @PostMapping("/logout")
    public Mono<ResponseEntity<Map<String, String>>> logout(
            @AuthenticationPrincipal OAuth2User principal,
            ServerWebExchange exchange) {
        return exchange.getSession()
                .flatMap(session -> {
                    session.invalidate();
                    Map<String, String> response = new HashMap<>();
                    response.put("message", "Logout successful");
                    return Mono.just(new ResponseEntity<>(response, HttpStatus.OK));
                });
    }
}
