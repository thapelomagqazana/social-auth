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
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;
import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class SaveBookmarkControllerTests {

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

    private String userToken;
    private String disabledUserToken;

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
        userToken = jwtUtils.generateToken(normalUser.getUsername(), 86400000, USER_ROLE);
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

        Mockito.when(bookmarkService.saveBookmark(Mockito.any(User.class), Mockito.anyString(), Mockito.anyString()))
        .thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            String title = invocation.getArgument(1);
            String url = invocation.getArgument(2);
            return new Bookmark(title, url, user); // Returns dynamically created bookmark
        });

        // Simulate duplicate bookmarks
        Mockito.when(bookmarkService.saveBookmark(Mockito.eq(normalUser), Mockito.eq("Duplicate Title"), Mockito.eq("https://example.com/duplicate")))
            .thenThrow(new IllegalArgumentException("Bookmark already exists"));

            
    }


    /**
     * TC_POS_001: User saves a valid bookmark.
     */
    @Test
    void testSaveValidBookmark() throws Exception {
        String requestBody = """
            {
                "title": "Sample Image",
                "url": "https://example.com/image.jpg"
            }
        """;

        mockMvc.perform(post("/api/bookmarks")
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestBody))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.bookmark.title").value("Sample Image"))
            .andExpect(jsonPath("$.bookmark.url").value("https://example.com/image.jpg"))
            .andExpect(jsonPath("$.bookmark.createdAt").exists());

    }

    /**
     * TC_POS_002: User saves a bookmark with a long but valid title.
     */
    @Test
    void testSaveBookmarkWithLongTitle() throws Exception {
        String requestBody = """
            {
                "title": "This is a very long title that is still within limit",
                "url": "https://example.com/image.jpg"
            }
        """;

        mockMvc.perform(post("/api/bookmarks")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookmark.title").value("This is a very long title that is still within limit"))
                .andExpect(jsonPath("$.bookmark.url").value("https://example.com/image.jpg"));
    }

    /**
     * TC_POS_003: User saves a bookmark with a long but valid URL.
     */
    @Test
    void testSaveBookmarkWithLongURL() throws Exception {
        String longUrl = "https://example.com/" + "a".repeat(480);
        String requestBody = """
            {
                "title": "Long URL",
                "url": "%s"
            }
        """.formatted(longUrl);

        mockMvc.perform(post("/api/bookmarks")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookmark.title").value("Long URL"))
                .andExpect(jsonPath("$.bookmark.url").value(longUrl));
    }

    /**
     * TC_POS_004: User saves a bookmark with special characters in the title.
     */
    @Test
    void testSaveBookmarkWithSpecialCharacters() throws Exception {
        String requestBody = """
            {
                "title": "Cool! Image @2024",
                "url": "https://example.com/image.jpg"
            }
        """;

        mockMvc.perform(post("/api/bookmarks")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookmark.title").value("Cool! Image @2024"))
                .andExpect(jsonPath("$.bookmark.url").value("https://example.com/image.jpg"));
    }

    /**
     * TC_POS_005: User saves a bookmark with a mixed-case URL.
     */
    @Test
    void testSaveBookmarkWithMixedCaseURL() throws Exception {
        String requestBody = """
            {
                "title": "Mixed Case URL",
                "url": "HTTPS://EXAMPLE.COM/IMAGE.JPG"
            }
        """;

        mockMvc.perform(post("/api/bookmarks")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookmark.title").value("Mixed Case URL"))
                .andExpect(jsonPath("$.bookmark.url").value("HTTPS://EXAMPLE.COM/IMAGE.JPG"));
    }

    /**
     * TC_NEG_001: No authentication token provided.
     */
    @Test
    void testNoAuthToken() throws Exception {
        String requestBody = """
            {
                "title": "Sample Image",
                "url": "https://example.com/image.jpg"
            }
        """;

        mockMvc.perform(post("/api/bookmarks")
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestBody))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.message").value("Token is missing."));
    }

    /**
     * TC_NEG_002: Invalid authentication token used.
     */
    @Test
    void testInvalidAuthToken() throws Exception {
        mockMvc.perform(post("/api/bookmarks")
            .header(HttpHeaders.AUTHORIZATION, "Bearer invalid_token")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{}"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.message").value("Invalid token."));
    }

    /**
     * TC_NEG_003: Missing title field.
     */
    @Test
    void testMissingTitle() throws Exception {
        String requestBody = """
            {
                "url": "https://example.com/image.jpg"
            }
        """;

        mockMvc.perform(post("/api/bookmarks")
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestBody))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors.title").value("Title is required."));
    }

    /**
     * TC_NEG_004: Missing URL field.
     */
    @Test
    void testMissingUrl() throws Exception {
        String requestBody = """
            {
                "title": "Sample Image"
            }
        """;

        mockMvc.perform(post("/api/bookmarks")
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestBody))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors.url").value("URL is required."));
    }

    /**
     * TC_NEG_005: Title exceeds character limit.
     */
    @Test
    void testTitleTooLong() throws Exception {
        String requestBody = """
            {
                "title": "%s",
                "url": "https://example.com/image.jpg"
            }
        """.formatted("a".repeat(101));

        mockMvc.perform(post("/api/bookmarks")
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestBody))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors.title").value("Title must not exceed 100 characters."));
    }

    /**
     * TC_NEG_006: URL exceeds character limit.
     */
    @Test
    void testUrlTooLong() throws Exception {
        String requestBody = """
            {
                "title": "Valid Title",
                "url": "%s"
            }
        """.formatted("https://example.com/" + "a".repeat(501));

        mockMvc.perform(post("/api/bookmarks")
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestBody))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors.url").value("URL length must not exceed 500 characters."));
    }

    /**
     * TC_NEG_009: Duplicate bookmark.
     */
    @Test
    void testDuplicateBookmark() throws Exception {
        String requestBody = """
            {
                "title": "Duplicate Title",
                "url": "https://example.com/duplicate"
            }
        """;

        mockMvc.perform(post("/api/bookmarks")
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestBody))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("Bookmark already exists"));
    }

    /**
     * TC_NEG_010: User saves a bookmark while disabled.
     */
    @Test
    void testDisabledUserCannotSaveBookmark() throws Exception {
        String requestBody = """
            {
                "title": "Disabled User Bookmark",
                "url": "https://example.com/image.jpg"
            }
        """;

        mockMvc.perform(post("/api/bookmarks")
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + disabledUserToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestBody))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.message").value("User account is disabled"));
    }

    /**
     * TC_EDGE_001: Title contains only spaces.
     */
    @Test
    void testTitleContainsOnlySpaces() throws Exception {
        String requestBody = """
            {
                "title": " ",
                "url": "https://example.com/image.jpg"
            }
        """;

        mockMvc.perform(post("/api/bookmarks")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.title").value("Title is required."));
    }

    /**
     * TC_EDGE_002: URL contains extra spaces.
     */
    @Test
    void testUrlContainsExtraSpaces() throws Exception {
        String requestBody = """
            {
                "title": "Valid Title",
                "url": " https://example.com/image.jpg "
            }
        """;

        mockMvc.perform(post("/api/bookmarks")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookmark.title").value("Valid Title"))
                .andExpect(jsonPath("$.bookmark.url").value("https://example.com/image.jpg")); // URL should be trimmed
    }

    /**
     * TC_EDGE_003: Title contains emojis.
     */
    @Test
    void testTitleContainsEmojis() throws Exception {
        String requestBody = """
            {
                "title": "🚀 Best Image",
                "url": "https://example.com/image.jpg"
            }
        """;

        mockMvc.perform(post("/api/bookmarks")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookmark.title").value("🚀 Best Image"))
                .andExpect(jsonPath("$.bookmark.url").value("https://example.com/image.jpg"));
    }

    /**
     * TC_EDGE_004: URL contains query parameters.
     */
    @Test
    void testUrlContainsQueryParameters() throws Exception {
        String requestBody = """
            {
                "title": "Image with Params",
                "url": "https://example.com/image.jpg?size=large&ref=abc"
            }
        """;

        mockMvc.perform(post("/api/bookmarks")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookmark.title").value("Image with Params"))
                .andExpect(jsonPath("$.bookmark.url").value("https://example.com/image.jpg?size=large&ref=abc"));
    }

    /**
     * TC_EDGE_005: User bookmarks the same URL but with different titles.
     */
    @Test
    void testBookmarkSameUrlWithDifferentTitles() throws Exception {
        String requestBody1 = """
            {
                "title": "First Title",
                "url": "https://example.com/shared-url"
            }
        """;

        String requestBody2 = """
            {
                "title": "Second Title",
                "url": "https://example.com/shared-url"
            }
        """;

        mockMvc.perform(post("/api/bookmarks")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookmark.title").value("First Title"))
                .andExpect(jsonPath("$.bookmark.url").value("https://example.com/shared-url"));

        mockMvc.perform(post("/api/bookmarks")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookmark.title").value("Second Title"))
                .andExpect(jsonPath("$.bookmark.url").value("https://example.com/shared-url"));
    }

}
