package com.example.backend.controller;

import com.example.backend.model.User;
import com.example.backend.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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

        // ✅ Ensure usersPage is not null before calling `.isEmpty()`
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
}
