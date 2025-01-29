package com.example.backend.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Utility class for JWT operations.
 */
@Component
public class JwtUtils {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expirationMs;

    private final JwtBlacklistService jwtBlacklistService;

    private final Set<String> revokedTokens = new HashSet<>(); // Store revoked tokens

    public JwtUtils(JwtBlacklistService jwtBlacklistService) {
        this.jwtBlacklistService = jwtBlacklistService;
    }

    /**
     * Generates a JWT token for a given username.
     */
    public String generateToken(String username, long expirationMillis, String role) {
        Date expirationDate = new Date(System.currentTimeMillis() + expirationMillis);
    
        return Jwts.builder()
                .setSubject(username)
                .claim("authorities", List.of(role))  // ✅ Store role under "authorities" claim
                .setIssuedAt(new Date())
                .setExpiration(expirationDate)
                .signWith(SignatureAlgorithm.HS256, secret)
                .compact();
    }
    
    
    

    /**
     * Validates a JWT token.
     */
    public boolean validateToken(String token) {
        if (jwtBlacklistService.isTokenBlacklisted(token)) {
            return false;
        }

        // if (isTokenRevoked(token)) {
        //     return false;
        // }

        try {
            Jwts.parser().setSigningKey(secret).parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Extracts the username from a token.
     */
    public String extractUsername(String token) {
        return parseClaims(token).getSubject();
    }

    /**
     * Extracts the expiration time from a token.
     */
    public Date extractExpiration(String token) {
        return parseClaims(token).getExpiration();
    }

    /**
     * Parses claims from a token.
     */
    private Claims parseClaims(String token) {
        return Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody();
    }

    public String getSecret() {
        return secret;
    }

    
    /**
     * Revokes a token by adding it to the revoked list.
     */
    public void revokeToken(String token) {
        revokedTokens.add(token);
    }

    /**
     * Checks if a token is revoked.
     */
    public boolean isTokenRevoked(String token) {
        return revokedTokens.contains(token);
    }
}