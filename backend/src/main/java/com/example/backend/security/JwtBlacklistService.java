package com.example.backend.security;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import java.util.concurrent.TimeUnit;

/**
 * Service for managing blacklisted JWT tokens using Redis.
 */
@Service
public class JwtBlacklistService {

    private final StringRedisTemplate redisTemplate;

    public JwtBlacklistService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * Blacklists a JWT token in Redis with an expiration time.
     *
     * @param token The JWT token to blacklist.
     * @param expirationMs The expiration time in milliseconds.
     */
    public void blacklistToken(String token, long expirationMs) {
        if (token == null || token.isEmpty() || expirationMs <= 0) {
            // System.out.println("❌ Skipping invalid token blacklisting.");
            return;
        }
        // System.out.println("✅ Blacklisting token for " + expirationMs + "ms");
        redisTemplate.opsForValue().set(token, "BLACKLISTED", expirationMs, TimeUnit.MILLISECONDS);
    }
    

    /**
     * Checks if a token is blacklisted.
     *
     * @param token The JWT token.
     * @return True if the token is blacklisted, false otherwise.
     */
    public boolean isTokenBlacklisted(String token) {
        return redisTemplate.hasKey(token);
    }
}
