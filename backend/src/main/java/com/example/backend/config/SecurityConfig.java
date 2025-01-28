package com.example.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuration class for Spring Security.
 */
@Configuration
public class SecurityConfig {

    /**
     * Configure a BCryptPasswordEncoder bean for encrypting passwords.
     * 
     * @return a BCryptPasswordEncoder instance
     */
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Configure the SecurityFilterChain bean for HTTP security.
     *
     * @param http the HttpSecurity object
     * @return the SecurityFilterChain instance
     * @throws Exception if configuration fails
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf().disable() // Disable CSRF for testing purposes
            .authorizeHttpRequests()
            .requestMatchers("/api/auth/**").permitAll() // Allow unauthenticated access to auth endpoints
            .anyRequest().authenticated();
        return http.build();
    }

    /**
     * Configure the authentication manager.
     * 
     * @param http the HttpSecurity object
     * @return the AuthenticationManager instance
     * @throws Exception if configuration fails
     */
    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        return http.getSharedObject(AuthenticationManager.class);
    }
}
