package com.example.api.controller;

import com.example.persistence.entity.User;
import com.example.service.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

/**
 * REST controller for managing User-related API endpoints.
 * Handles incoming HTTP requests for user operations.
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    /**
     * Constructor to inject the UserService.
     *
     * @param userService The service for handling user-related operations.
     */
    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Creates a new user.
     *
     * @param user The user entity to create.
     * @return A ResponseEntity containing the created user.
     */
    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user) {
        return ResponseEntity.ok(userService.saveUser(user));
    }

    /**
     * Retrieves a user by their email address.
     *
     * @param email The email address of the user.
     * @return A ResponseEntity containing the user if found, or a 404 status if not.
     */
    @GetMapping("/{email}")
    public ResponseEntity<User> getUserByEmail(@PathVariable String email) {
        Optional<User> user = userService.findByEmail(email);
        return user.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }
}
