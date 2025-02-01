package com.example.backend.controller;

import com.example.backend.dto.LoginRequest;
import com.example.backend.model.PasswordResetToken;
import com.example.backend.model.User;
import com.example.backend.security.JwtBlacklistService;
import com.example.backend.security.JwtUtils;
import com.example.backend.service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for user authentication and registration.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    // Constants for reusable literals
    private static final String MESSAGE = "message";
    private static final String INVALID_CREDENTIALS = "Invalid username or password.";
    private static final String ACCOUNT_DISABLED = "Account is disabled.";

    private final String BASE_URL = System.getProperty("BASE_URL");

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private JwtBlacklistService jwtBlacklistService;

    /**
     * Registers a new user.
     *
     * @param user the user to register
     * @param result validation results
     * @return a success message or validation error message
     */
    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@Valid @RequestBody User user) {
        try {
            userService.registerUser(user);
            return ResponseEntity.ok("User registered successfully!");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(409).body(e.getMessage());
        }
    }

    /**
     * Log in a user and return a JWT token.
     *
     * @param loginRequest the login request containing username and password
     * @return a JWT token if authentication is successful
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> loginUser(@Valid @RequestBody LoginRequest loginRequest) {

        // Check payload size
        int payloadSize = loginRequest.toString().getBytes(StandardCharsets.UTF_8).length;
        int maxPayloadSize = 1024 * 1024; // 1MB
        if (payloadSize > maxPayloadSize) {
            return ResponseEntity.status(400).body(Map.of(MESSAGE, "Payload size exceeds the allowed limit."));
        }

        String username = loginRequest.getUsername().toLowerCase().trim();
        String password = loginRequest.getPassword();

        try {
            User user = userService.findByUsername(username);

            // Check if the account is disabled
            if (!user.isEnabled()) {
                return ResponseEntity.status(403).body(Map.of(MESSAGE, ACCOUNT_DISABLED));
            }

            // Validate password
            if (passwordEncoder.matches(password, user.getPassword())) {
                // Get the first role safely (or default to "ROLE_USER")
                String role = user.getRoles().isEmpty() ? "ROLE_USER" : user.getRoles().iterator().next();
                
                String token = jwtUtils.generateToken(username, 86400000, role);
                return ResponseEntity.ok(Map.of("token", "Bearer " + token));
            } else {
                return ResponseEntity.status(401).body(Map.of(MESSAGE, INVALID_CREDENTIALS));
            }
        } catch (RuntimeException ex) {
            return ResponseEntity.status(401).body(Map.of(MESSAGE, INVALID_CREDENTIALS));
        }
    }

    /**
     * Logs out the currently authenticated user by invalidating the JWT token.
     *
     * @param request The HTTP request containing the Authorization header.
     * @return A response indicating successful logout.
     */
    @PostMapping("/logout")
    public ResponseEntity<String> logoutUser(HttpServletRequest request) {
        String token = extractToken(request);

        if (token != null && jwtUtils.validateToken(token)) {
            Date expirationDate = jwtUtils.extractExpiration(token);
            long remainingExpiration = expirationDate.getTime() - System.currentTimeMillis();

            if (remainingExpiration > 0) {
                jwtBlacklistService.blacklistToken(token, remainingExpiration);
            } else {
                // System.out.println("❌ Skipping blacklist: Token already expired.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token is already expired.");
            }

            return ResponseEntity.ok("User logged out successfully.");
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or missing token.");
    }

    /**
     * Extracts the JWT token from the Authorization header.
     *
     * @param request The HTTP request.
     * @return The extracted token or null if not present.
     */
    private String extractToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }

        return null;
    }


    /**
     * Generates a password reset link and sends it via email.
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<?> requestPasswordReset(@RequestBody Map<String, String> request) {
        String email = request.get("email");

        try {
            if (email == null || email.trim().isEmpty()) {
                return ResponseEntity.status(400).body(Map.of(MESSAGE, "Email is required."));
            }
        
            // Check for spam (last request time)
            // if (userService.isSpamRequest(email)) {
            //     return ResponseEntity.status(429).body(Map.of(MESSAGE, "Too many requests. Try again later."));
            // }

            String token = userService.generatePasswordResetToken(email);
            String resetLink = BASE_URL + "/api/auth/reset-password?token=" + token;

            // Simulate email sending (Replace this with real email service)
            System.out.println("Password Reset Link: " + resetLink);

            return ResponseEntity.ok(Map.of(MESSAGE, "Password reset link sent to email."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(400).body(Map.of(MESSAGE, e.getMessage()));
        }
    }

    /**
     * Resets the user's password using a valid token.
     */
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> request) {
        String token = request.get("token");
        String newPassword = request.get("newPassword");

        
        try {
            String email = userService.validatePasswordResetToken(token);
            userService.updatePassword(email, newPassword);
            return ResponseEntity.ok(Map.of(MESSAGE, "Password has been reset successfully."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(400).body(Map.of(MESSAGE, e.getMessage()));
        }
    }
}
