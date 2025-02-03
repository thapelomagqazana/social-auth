package com.example.backend.controller;

import com.example.backend.model.Bookmark;
import com.example.backend.model.User;
import com.example.backend.service.BookmarkService;
import com.example.backend.service.UserService;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

/**
 * REST API Controller for managing user bookmarks.
 */
@Validated
@RestController
@RequestMapping("/api/bookmarks")
public class BookmarkController {

    private final BookmarkService bookmarkService;
    private final UserService userService;

    private static final String MESSAGE = "message";

    public BookmarkController(BookmarkService bookmarkService, UserService userService) {
        this.bookmarkService = bookmarkService;
        this.userService = userService;
    }

    /**
     * Save a new bookmark for the authenticated user.
     */
    @PostMapping
    public ResponseEntity<?> saveBookmark(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody Bookmark bookmarkRequest) {
    
        User user = userService.findByUsername(userDetails.getUsername());

        // Trim URL before processing
        String trimmedUrl = bookmarkRequest.getUrl().trim();
        bookmarkRequest.setUrl(trimmedUrl);

        try {
            if (user == null) {
                return ResponseEntity.status(401).body(Map.of(MESSAGE, "User not authenticated"));
            }
        
            if (!user.isEnabled()) {
                return ResponseEntity.status(403).body(Map.of(MESSAGE, "User account is disabled"));
            }
        
            Bookmark bookmark = bookmarkService.saveBookmark(user, bookmarkRequest.getTitle(), trimmedUrl);
        
            return ResponseEntity.ok(Map.of("bookmark", bookmark));
        } catch (IllegalArgumentException ex) { 
            return ResponseEntity.status(400).body(Map.of(MESSAGE, ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.status(500).body(Map.of(MESSAGE, "Bookmark could not be saved."));
        }
    }
    

    /**
     * Fetch all bookmarks for the authenticated user.
     */
    @GetMapping
    public ResponseEntity<?> getUserBookmarks(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByUsername(userDetails.getUsername());
        List<Bookmark> bookmarks = bookmarkService.getUserBookmarks(user);
        return ResponseEntity.ok(bookmarks);
    }

    /**
     * Delete a bookmark by ID.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteBookmark(
            @AuthenticationPrincipal UserDetails userDetails, @PathVariable Long id) {
        
        User user = userService.findByUsername(userDetails.getUsername());
        boolean deleted = bookmarkService.deleteBookmark(user, id);

        if (deleted) {
            return ResponseEntity.ok(Map.of("message", "Bookmark deleted successfully."));
        } else {
            return ResponseEntity.status(403).body(Map.of("message", "Bookmark not found or unauthorized."));
        }
    }
}
