package com.gomin.r2webflux.security;

import io.jsonwebtoken.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import java.util.Base64;
import java.util.Date;

import javax.crypto.spec.SecretKeySpec;
import java.security.Key;

@Slf4j
@Component
public class JwtTokenProvider {

    private Key key;
    private int jwtExpiration;

    @Value("${jwt.secret}")
    public void setJwtSecret(String secret) {
        // Base64로 인코딩된 키를 바이트 배열로 변환
        byte[] decodedKey = Base64.getDecoder().decode(secret);
        // HS512 알고리즘용 시크릿 키 생성
        this.key = new SecretKeySpec(decodedKey, SignatureAlgorithm.HS512.getJcaName());
        log.info("JWT Secret key has been set");
    }

    @Value("${jwt.expiration:86400000}")
    public void setJwtExpiration(int expiration) {
        this.jwtExpiration = expiration;
        log.info("JWT expiration has been set to: {} ms", expiration);
    }

    public String generateToken(Authentication authentication) {
        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
        
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpiration);

        return Jwts.builder()
                .setSubject(oauthToken.getName())
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
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
