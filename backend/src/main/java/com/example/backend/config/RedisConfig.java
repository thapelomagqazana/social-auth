package com.example.backend.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Configuration class for Redis caching.
 * Enables caching support and sets up the RedisTemplate.
 */
@Configuration
@EnableCaching  // Enable Spring's annotation-driven caching mechanism
public class RedisConfig {

    /**
     * Configure the Redis connection factory using Lettuce.
     * 
     * @return RedisConnectionFactory instance.
     */
    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        return new LettuceConnectionFactory(
            System.getProperty("REDIS_HOST", "localhost"), // Default to localhost
            Integer.parseInt(System.getProperty("REDIS_PORT", "6379")) // Default to 6379
        );
    }

    /**
     * Configure the RedisTemplate for interacting with Redis.
     * 
     * @param redisConnectionFactory the Redis connection factory.
     * @return RedisTemplate instance with JSON and String serializers.
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);

        // Use String serializer for keys
        template.setKeySerializer(new StringRedisSerializer());

        // Use JSON serializer for values
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());

        return template;
    }
}
