package com.example.backend.controller;

import com.example.backend.model.User;
import com.example.backend.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.Authentication;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
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
        try {
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
        catch (RuntimeException e) {
            return ResponseEntity.status(404).body(Map.of("message", e.getMessage()));
        }
    }

    /**
     * PUT /api/users/{id} - Update a specific user's profile.
     * 
     * Only the user themselves or an admin can update the profile.
     * 
     * @param id The user ID.
     * @param updates A map containing fields to be updated.
     * @param authentication The currently authenticated user.
     * @return ResponseEntity with updated user details.
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateUserProfile(
            @PathVariable Long id,
            @RequestBody Map<String, Object> updates,
            Authentication authentication) {
        
        try {
            String loggedInUsername = authentication.getName(); // Get currently logged-in user
            boolean isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(auth -> auth.getAuthority().equals("ADMIN"));

            User user = userService.findById(id);
            if (user == null) {
                return ResponseEntity.status(404).body(Map.of("message", "User not found"));
            }

            // Only allow the user themselves OR an admin to update the profile
            if (!loggedInUsername.equals(user.getUsername()) && !isAdmin) {
                return ResponseEntity.status(403).body(Map.of("message", "Access denied."));
            }

            // Update user fields dynamically
            userService.updateUser(id, updates, isAdmin);
            return ResponseEntity.ok(Map.of("message", "User profile updated successfully", "user", user));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(400).body(Map.of("message", e.getMessage())); // Return 400 for duplicate email/username
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(Map.of("message", e.getMessage()));
        }
    }

    /**
     * DELETE /api/users/{id} - Delete a user (Admin only).
     *
     * @param id The ID of the user to delete.
     * @param authentication The authenticated user making the request.
     * @return ResponseEntity with a success or error message.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id, Authentication authentication) {
        try {
            boolean isAdmin = authentication.getAuthorities().stream()
                            .anyMatch(auth -> auth.getAuthority().equals("ADMIN"));
            
            // Prevent standard users from deleting accounts
            if (!isAdmin) {
                return ResponseEntity.status(403).body("{\"message\": \"Access denied.\"}");
            }

            String loggedInUsername = authentication.getName(); // Get the current admin user

            boolean isDeleted = userService.deleteUser(id, loggedInUsername);
            
            if (!isDeleted) {
                return ResponseEntity.status(400).body("{\"message\": \"Failed to delete user.\"}");
            }
    
            return ResponseEntity.ok("{\"message\": \"User deleted successfully.\"}");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(400).body(Map.of("message", e.getMessage())); // Return 400 for duplicate email/username
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(Map.of("message", e.getMessage()));
        }
    }

    /**
     * GET /api/users/me - Fetch the currently logged-in user's profile.
     *
     * This endpoint allows any authenticated user to retrieve their own profile information.
     *
     * @param authentication The authenticated user from the security context.
     * @return The user profile data in JSON format.
     */
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUserProfile(Authentication authentication) {
        try {
            // Get the username from the authenticated user
            String loggedInUsername = authentication.getName();

            // Fetch user details from the database
            User user = userService.findByUsername(loggedInUsername);
            if (user == null) {
                return ResponseEntity.status(404).body("{\"message\": \"User not found\"}");
            }

            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(Map.of("message", e.getMessage()));
        }
        
    }
}
