package com.gomin.r2webflux.controller;

import com.gomin.r2webflux.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final JwtTokenProvider jwtTokenProvider;

    @GetMapping("/user")
    public Mono<ResponseEntity<Map<String, Object>>> getUser(@AuthenticationPrincipal OAuth2User principal) {
        Map<String, Object> response = new HashMap<>();
        if (principal != null) {
            response.put("name", principal.getName());
            response.put("attributes", principal.getAttributes());
        }
        return Mono.just(new ResponseEntity<>(response, HttpStatus.OK));
    }

    @GetMapping("/login/success")
    public Mono<ResponseEntity<Map<String, String>>> loginSuccess(@AuthenticationPrincipal OAuth2User principal) {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Login successful");
        response.put("email", principal.getAttribute("email"));
        return Mono.just(new ResponseEntity<>(response, HttpStatus.OK));
    }
}
