package com.gomin.r2webflux.security;

import io.jsonwebtoken.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Date;

@Slf4j
@Component
public class JwtTokenProvider {

    private String jwtSecret;
    private int jwtExpiration;
    
    @Value("${jwt.secret}")
    public void setJwtSecret(String secret) {
        this.jwtSecret = secret;
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
                .signWith(SignatureAlgorithm.HS512, jwtSecret)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token);
            return true;
        } catch (SignatureException ex) {
            log.error("Invalid JWT signature");
        } catch (MalformedJwtException ex) {
            log.error("Invalid JWT token");
        } catch (ExpiredJwtException ex) {
            log.error("Expired JWT token");
        } catch (UnsupportedJwtException ex) {
            log.error("Unsupported JWT token");
        } catch (IllegalArgumentException ex) {
            log.error("JWT claims string is empty");
        }
        return false;
    }

    public Mono<Void> onAuthenticationSuccess(WebFilterExchange webFilterExchange, Authentication authentication) {
        String token = generateToken(authentication);
        webFilterExchange.getExchange().getResponse()
                .getHeaders()
                .add("Authorization", "Bearer " + token);
        return Mono.empty();
    }
}
