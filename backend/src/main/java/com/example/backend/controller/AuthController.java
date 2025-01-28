package com.example.backend.controller;

import com.example.backend.model.User;
import com.example.backend.security.JwtUtils;
import com.example.backend.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for user authentication and registration.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    /**
     * Registers a new user.
     *
     * @param user the user to register
     * @param result validation results
     * @return a success message or validation error message
     */
    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@Valid @RequestBody User user, BindingResult result) {
        if (result.hasErrors()) {
            String errorMessage = result.getAllErrors().get(0).getDefaultMessage();
            return ResponseEntity.badRequest().body(errorMessage);
        }
    
        try {
            userService.registerUser(user);
            return ResponseEntity.ok("User registered successfully!");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(409).body(e.getMessage());
        }
    }
    

    /**
     * Logs in a user and generates a JWT.
     *
     * @param username the username
     * @param password the password
     * @return a JWT or an error message
     */
    @PostMapping("/login")
    public ResponseEntity<String> loginUser(@RequestParam String username, @RequestParam String password) {
        try {
            // Retrieve the user by username
            User user = userService.findByUsername(username);

            // Check if the provided password matches the encoded password
            if (passwordEncoder.matches(password, user.getPassword())) {
                // Generate a JWT token
                String token = jwtUtils.generateToken(username);

                // Return the token
                return ResponseEntity.ok("Bearer " + token);
            } else {
                // Invalid password
                return ResponseEntity.status(401).body("Invalid username or password.");
            }
        } catch (RuntimeException ex) {
            // User not found or other issues
            return ResponseEntity.status(401).body("Invalid username or password.");
        }
    }
}
