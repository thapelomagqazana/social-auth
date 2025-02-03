package com.example.backend.bookmark;

import com.example.backend.model.Bookmark;
import com.example.backend.model.User;
import com.example.backend.repository.BookmarkRepository;
import com.example.backend.repository.UserRepository;
import com.example.backend.security.JwtUtils;
import com.example.backend.security.UserDetailsServiceImpl;
import com.example.backend.service.BookmarkService;
import com.example.backend.service.UserService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class GetBookmarksControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtUtils jwtUtils;

    @MockBean
    private BookmarkService bookmarkService;

    @MockBean
    private UserService userService;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    private String validUserToken;
    private String disabledUserToken;
    private String expiredToken = "expired.jwt.token";
    private String malformedToken = "invalid.jwt.token";

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    
    private final String USER_ROLE = "USER";

    @BeforeEach
    void setUp() {
        User normalUser = new User(5L, "normalUser", "user@example.com", passwordEncoder.encode("User@123"), Set.of(USER_ROLE));
        User disabledUser = new User(6L, "disabledUser", "disabled@example.com", passwordEncoder.encode("User@123"), Set.of(USER_ROLE));
        disabledUser.setEnabled(false);

        // Mock repository responses
        Mockito.when(userRepository.findByUsername("normalUser")).thenReturn(Optional.of(normalUser));
        Mockito.when(userService.findByUsername("normalUser")).thenReturn(normalUser);
        Mockito.when(userRepository.findByUsername("disabledUser")).thenReturn(Optional.of(disabledUser));
        Mockito.when(userService.findByUsername("disabledUser")).thenReturn(disabledUser);

        // Generate JWT Tokens
        validUserToken = jwtUtils.generateToken(normalUser.getUsername(), 86400000, USER_ROLE);
        disabledUserToken = jwtUtils.generateToken(disabledUser.getUsername(), 86400000, USER_ROLE);
        
        // Mock UserDetailsService
        Mockito.when(userDetailsService.loadUserByUsername("normalUser"))
            .thenReturn(org.springframework.security.core.userdetails.User.withUsername("normalUser")
                .password(normalUser.getPassword())
                .roles(USER_ROLE)
                .build());

        Mockito.when(userDetailsService.loadUserByUsername("disabledUser"))
            .thenReturn(org.springframework.security.core.userdetails.User.withUsername("disabledUser")
                .password(normalUser.getPassword())
                .roles(USER_ROLE)
                .build());

        // Mock when user is deleted
        Mockito.when(userRepository.findByUsername("deletedUser")).thenReturn(Optional.empty());
        Mockito.when(userService.findByUsername("deletedUser")).thenThrow(new RuntimeException("User not found"));

        // Mock bookmarks data
        Bookmark bookmark1 = new Bookmark("Sample Image", "https://example.com/image.jpg", normalUser);
        Bookmark bookmark2 = new Bookmark("Special Char 💡 Bookmark", "https://example.com/image.jpg", normalUser);
        Bookmark bookmark3 = new Bookmark("Query Params", "https://example.com/image.jpg?size=large&ref=abc", normalUser);

        Mockito.when(bookmarkService.getUserBookmarks(normalUser))
            .thenReturn(List.of(bookmark1, bookmark2, bookmark3));
    }

    /**
     * TC_POS_001: User fetches bookmarks successfully.
     */
    @Test
    void testFetchBookmarksSuccessfully() throws Exception {
        mockMvc.perform(get("/api/bookmarks")
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + validUserToken)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(3))
            .andExpect(jsonPath("$[0].title").value("Sample Image"))
            .andExpect(jsonPath("$[1].title").value("Special Char 💡 Bookmark"))
            .andExpect(jsonPath("$[2].url").value("https://example.com/image.jpg?size=large&ref=abc"));
    }

    /**
     * TC_POS_002: User has no bookmarks saved.
     */
    @Test
    void testFetchBookmarksWhenNoneExist() throws Exception {
        Mockito.when(bookmarkService.getUserBookmarks(Mockito.any(User.class))).thenReturn(List.of());

        mockMvc.perform(get("/api/bookmarks")
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + validUserToken)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(0));
    }

    /**
     * TC_POS_003: User fetches bookmarks with special characters in title.
     */
    @Test
    void testFetchBookmarksWithSpecialCharactersInTitle() throws Exception {
        mockMvc.perform(get("/api/bookmarks")
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + validUserToken)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[1].title").value("Special Char 💡 Bookmark"));
    }

    /**
     * TC_POS_004: User fetches bookmarks with query parameters in URL.
     */
    @Test
    void testFetchBookmarksWithQueryParameters() throws Exception {
        mockMvc.perform(get("/api/bookmarks")
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + validUserToken)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[2].url").value("https://example.com/image.jpg?size=large&ref=abc"));
    }

    /**
     * TC_NEG_001: No authentication token provided.
     */
    @Test
    void testFetchBookmarksWithoutAuthToken() throws Exception {
        mockMvc.perform(get("/api/bookmarks")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.message").value("Token is missing."));
    }

    /**
     * TC_NEG_002: Invalid JWT token used.
     */
    @Test
    void testFetchBookmarksWithInvalidToken() throws Exception {
        mockMvc.perform(get("/api/bookmarks")
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + malformedToken)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.message").value("Invalid token."));
    }

    /**
     * TC_NEG_003: User account is disabled.
     */
    @Test
    void testFetchBookmarksWithDisabledUser() throws Exception {
        mockMvc.perform(get("/api/bookmarks")
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + disabledUserToken)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.message").value("User account is disabled"));
    }

    /**
     * TC_NEG_004: JWT token has expired.
     */
    @Test
    void testFetchBookmarksWithExpiredToken() throws Exception {
        mockMvc.perform(get("/api/bookmarks")
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + expiredToken)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.message").value("Invalid token."));
    }

    /**
     * TC_NEG_005: User attempts to fetch bookmarks but is deleted from the database.
     */
    @Test
    void testFetchBookmarksWithDeletedUser() throws Exception {
        mockMvc.perform(get("/api/bookmarks")
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtUtils.generateToken("deletedUser", 86400000, USER_ROLE))
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.message").value("User no longer exists."));
    }
}
