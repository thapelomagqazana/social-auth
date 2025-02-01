package com.example.backend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * JWT Authentication Filter for processing token-based authentication.
 */
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final UserDetailsService userDetailsService;
    private final JwtBlacklistService jwtBlacklistService;

    @Autowired
    public JwtAuthFilter(JwtUtils jwtUtils, @Lazy UserDetailsService userDetailsService, @Lazy JwtBlacklistService jwtBlacklistService) {
        this.jwtUtils = jwtUtils;
        this.userDetailsService = userDetailsService;
        this.jwtBlacklistService = jwtBlacklistService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String requestURI = request.getRequestURI();

        // Skip JWT validation for login and register endpoints
        if (requestURI.startsWith("/api/auth/login") || requestURI.startsWith("/api/auth/register") ||
            requestURI.startsWith("/api/auth/reset-password") || requestURI.startsWith("/api/auth/forgot-password")) {
            chain.doFilter(request, response);
            return;
        }
    
        String token = extractToken(request);
    
        if (token == null || token.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"message\": \"Token is missing.\"}");
            return;
        }
    
        if (!jwtUtils.validateToken(token)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"message\": \"Invalid token.\"}");
            return;
        }
    
        if (jwtBlacklistService.isTokenBlacklisted(token)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"message\": \"Token is already revoked.\"}");
            return;
        }
    
        String username = jwtUtils.extractUsername(token);
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
    
        // Use jwtUtils.getSecret() instead of accessing 'secret' directly
        Claims claims = Jwts.parser()
                .setSigningKey(jwtUtils.getSecret())  // ✅ Retrieve secret from JwtUtils
                .parseClaimsJws(token)
                .getBody();
    
        List<String> roles = claims.get("authorities", List.class);  // Read role from JWT
    
        if (userDetails == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"message\": \"User no longer exists.\"}");
            return;
        }
    
        // Convert roles to Spring Security authorities
        List<SimpleGrantedAuthority> authorities = roles.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                userDetails, null, authorities);
        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
    
        SecurityContextHolder.getContext().setAuthentication(authToken);
    
        chain.doFilter(request, response);
    }
    

    private String extractToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        return (authHeader != null && authHeader.startsWith("Bearer ")) ? authHeader.substring(7) : null;
    }
}
