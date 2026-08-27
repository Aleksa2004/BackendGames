package com.example.games_extractor.config;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
//Ova klasa omogucuje da server zna koje je korisnik nakon login-a,tj da ne mora slati pri svakom zathjevu username i password
@Component
public class JwtUtil {

    private final Key key ;

    // Token važi 24 sata
    private final long EXPIRATION = 1000L * 60 * 60 * 24;
    
    public JwtUtil(@Value("${jwt.secret}") String secret) {
    	this.key=Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    // Pravi token za korisnika
    public String generateToken(String username, Long userId, Long tenantId,String tenantName) {

        return Jwts.builder()
                .setSubject(username)
                .claim("userId", userId)
                .claim("tenantId", tenantId)
                .claim("tenantName", tenantName)
                .setIssuedAt(new Date())
                .setExpiration(
                        new Date(System.currentTimeMillis() + EXPIRATION)
                )
                .signWith(key)
                .compact();
    }

    // Iz tokena uzima username
    public String extractUsername(String token) {

        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    // Iz tokena uzima userId
    public Long extractUserId(String token) {

        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get("userId", Long.class);
    }

    // Iz tokena uzima tenantId
    public Long extractTenantId(String token) {

        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get("tenantId", Long.class);
    }

    // Provjerava da li je token validan
    public boolean validateToken(String token) {

        try {
            extractUsername(token);
            return true;

        } catch (Exception e) {
            return false;
        }
    }
}