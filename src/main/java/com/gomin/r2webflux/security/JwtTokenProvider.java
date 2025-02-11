package com.gomin.r2webflux.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Slf4j
@Component
public class JwtTokenProvider {

    private SecretKey key;
    private int jwtExpiration;

    @Value("${jwt.secret}")
    public void setJwtSecret(String secret) {
        // HMAC-SHA 알고리즘에 적합한 키 생성
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        log.info("JWT Secret key has been set");
    }

    @Value("${jwt.expiration:86400000}")
    public void setJwtExpiration(int expiration) {
        this.jwtExpiration = expiration;
    }

    public String generateToken(Authentication authentication) {
        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpiration);

        try {
            String token = Jwts.builder()
                    .setSubject(oauthToken.getName())
                    .setIssuedAt(now)
                    .setExpiration(expiryDate)
                    .signWith(key)  // 알고리즘 자동 선택
                    .compact();
            log.info("Generated JWT token for user: {}", oauthToken.getName());
            return token;
        } catch (Exception e) {
            log.error("Token generation failed: {}", e.getMessage());
            throw new RuntimeException("Could not generate token", e);
        }
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token);
            return true;
        } catch (SecurityException | MalformedJwtException | ExpiredJwtException | UnsupportedJwtException | IllegalArgumentException ex) {
            log.error("JWT validation failed: {}", ex.getMessage());
            return false;
        }
    }

    public Mono<Void> onAuthenticationSuccess(WebFilterExchange webFilterExchange, Authentication authentication) {
        String token = generateToken(authentication);
        webFilterExchange.getExchange().getResponse()
                .getHeaders()
                .add("Authorization", "Bearer " + token);
        return Mono.empty();
    }
}
