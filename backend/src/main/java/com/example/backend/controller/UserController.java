package com.example.backend.controller;

import com.example.backend.model.User;
import com.example.backend.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.Authentication;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * REST Controller for managing users.
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    /**
     * Constructor-based dependency injection for UserService.
     *
     * @param userService the user service to manage user operations.
     */
    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * GET /api/users - Fetch all users (Admin-only) with optional pagination and sorting.
     *
     * If no pagination parameters (`page`, `size`) are provided, all users are returned.
     * If pagination parameters are present, paginated results are returned.
     *
     * @param pageable Pageable object to support pagination and sorting.
     * @param page Optional page number.
     * @param size Optional page size.
     * @return a list of users or paginated users based on query parameters.
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')") // Restrict access to admins
    public ResponseEntity<?> getUsers(
            @PageableDefault(page = 0, size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {

        // If no pagination parameters are provided, return all users
        if (page == null && size == null) {
            List<User> users = userService.getAllUsers();

            if (users.isEmpty()) {
                return ResponseEntity.ok(Map.of("message", "No users found."));
            }

            return ResponseEntity.ok(users);
        }

        // Ensure usersPage is not null before calling `.isEmpty()`
        Page<User> usersPage = userService.getAllUsers(pageable);
        if (usersPage == null) {
            return ResponseEntity.status(500).body(Map.of("error", "Unexpected server error while fetching users."));
        }

        if (usersPage.isEmpty()) {
            return ResponseEntity.ok(Map.of("message", "No users found.", "totalPages", 0));
        }

        return ResponseEntity.ok(Map.of(
            "users", usersPage.getContent(),
            "currentPage", usersPage.getNumber(),
            "totalPages", usersPage.getTotalPages(),
            "totalUsers", usersPage.getTotalElements()
        ));
    }


    /**
     * GET /api/users/{id} - Fetch a specific user's profile.
     * 
     * Only allow:
     * - The logged-in user to fetch their own profile
     * - Admins to fetch any user profile
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id, Authentication authentication) {
        String loggedInUsername = authentication.getName(); // Get currently logged-in user
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ADMIN"));

        User user = userService.findById(id);
        if (user == null) {
            return ResponseEntity.status(404).body("{\"message\": \"User not found\"}");
        }

        // Allow only the logged-in user OR an admin to access
        if (!loggedInUsername.equals(user.getUsername()) && !isAdmin) {
            return ResponseEntity.status(403).body("{\"message\": \"Access denied.\"}");
        }

        return ResponseEntity.ok(user);
    }

    /**
     * PUT /api/users/{id} - Update a specific user's profile.
     * 
     * Only allow:
     * - The logged-in user to update their own profile
     * - Admins to update any user profile
     *
     * @param id The ID of the user to update.
     * @param updates A map containing the fields to be updated.
     * @param authentication The authentication object representing the logged-in user.
     * @return A ResponseEntity indicating success or failure.
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateUserProfile(
            @PathVariable Long id,
            @RequestBody Map<String, Object> updates,
            Authentication authentication) {
        
        String loggedInUsername = authentication.getName(); // Get currently logged-in user
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ADMIN"));

        // Fetch the user to update
        User existingUser = userService.findById(id);
        if (existingUser == null) {
            return ResponseEntity.status(404).body(Map.of("message", "User not found"));
        }

        // Allow only the logged-in user OR an admin to update
        if (!loggedInUsername.equals(existingUser.getUsername()) && !isAdmin) {
            return ResponseEntity.status(403).body(Map.of("message", "Access denied"));
        }

        // Update allowed fields dynamically
        updates.forEach((key, value) -> {
            switch (key) {
                case "username":
                    existingUser.setUsername(value.toString().trim());
                    break;
                case "email":
                    existingUser.setEmail(value.toString().trim().toLowerCase());
                    break;
                case "password":
                    existingUser.setPassword(userService.encodePassword(value.toString())); // Encrypt password
                    break;
                case "roles":
                    if (isAdmin) { // Only admins can update roles
                        existingUser.setRoles((Set<String>)value);
                    }
                    break;
            }
        });

        // Save updated user
        User updatedUser = userService.saveUser(existingUser);
        return ResponseEntity.ok(Map.of("message", "User updated successfully", "user", updatedUser));
    }

}
