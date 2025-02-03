package com.example.backend.service;

import com.example.backend.model.Bookmark;
import com.example.backend.model.User;
import com.example.backend.repository.BookmarkRepository;
import com.example.backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

/**
 * Service layer for managing bookmark operations.
 */
@Service
public class BookmarkService {

    private final BookmarkRepository bookmarkRepository;
    private final UserRepository userRepository;

    public BookmarkService(BookmarkRepository bookmarkRepository, UserRepository userRepository) {
        this.bookmarkRepository = bookmarkRepository;
        this.userRepository = userRepository;
    }

    /**
     * Save a new bookmark for a user.
     * @param user The user saving the bookmark.
     * @param title The title of the bookmarked content.
     * @param url The URL of the bookmarked content.
     * @return The saved bookmark.
     */
    public Bookmark saveBookmark(User user, String title, String url) {
        if (bookmarkRepository.existsByUserAndTitleAndUrl(user, title, url)) {
            throw new IllegalArgumentException("Bookmark already exists");
        }
    
        Bookmark bookmark = new Bookmark(title, url, user);
        return bookmarkRepository.save(bookmark);
    }

    /**
     * Retrieve all bookmarks for a user.
     * @param user The user whose bookmarks should be fetched.
     * @return List of bookmarks.
     */
    public List<Bookmark> getUserBookmarks(User user) {
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new RuntimeException("User not found");
        }
        return bookmarkRepository.findByUser(user);
    }

    /**
     * Delete a bookmark by ID if it belongs to the user.
     * @param user The user requesting the deletion.
     * @param bookmarkId The ID of the bookmark.
     * @return true if deleted successfully, false otherwise.
     */
    @Transactional
    public boolean deleteBookmark(User user, Long bookmarkId) {
        Optional<Bookmark> bookmarkOpt = bookmarkRepository.findByIdAndUser(bookmarkId, user);
        if (bookmarkOpt.isPresent()) {
            bookmarkRepository.delete(bookmarkOpt.get());
            return true;
        }
        return false;
    }

    /**
     * Fetch a bookmark by ID for a specific user.
     * @param id The ID of the bookmark.
     * @param user The authenticated user.
     * @return Optional containing the bookmark if found.
     */
    public Optional<Bookmark> getBookmarkByIdAndUser(Long id, User user) {
        if (bookmarkRepository.findById(id).isPresent()){
            throw new RuntimeException("Bookmark not found");
        }

        if (userRepository.findByEmail(user.getEmail()).isPresent()){
            throw new RuntimeException("User not found");
        }
        return bookmarkRepository.findByIdAndUser(id, user);
    }
}
