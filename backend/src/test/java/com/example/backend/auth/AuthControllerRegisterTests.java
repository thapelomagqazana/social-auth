package com.example.backend.auth;

import com.example.backend.model.User;
import com.example.backend.repository.UserRepository;
import com.example.backend.security.JwtUtils;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class AuthControllerRegisterTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private JwtUtils jwtUtils;

    @Autowired
    private UserRepository userRepository;

    private final String ROLE = "ROLE_USER";

    @BeforeEach
    void setUp() {
        // Clear the database before each test
        userRepository.deleteAll();
    }


    @Test
    @DisplayName("TC_POS_001: Register a new user successfully")
    void testRegisterNewUserSuccessfully() throws Exception {
        User user = new User();
        user.setUsername("newuser");
        user.setEmail("newuser@example.com");
        user.setPassword("Password@123");

        // Mock JWTUtils behavior (if applicable)
        when(jwtUtils.generateToken(user.getUsername(), 86400000, ROLE)).thenReturn("mockToken");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isOk())
                .andExpect(content().string("User registered successfully!"));
    }

    @Test
    @DisplayName("TC_POS_002: Register a user with a strong password")
    void testRegisterUserWithStrongPassword() throws Exception {
        User user = new User();
        user.setUsername("secureuser");
        user.setEmail("secure@example.com");
        user.setPassword("StrongPassword@2023");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isOk())
                .andExpect(content().string("User registered successfully!"));
    }

    @Test
    @DisplayName("TC_POS_003: Register a user with minimal valid details")
    void testRegisterUserWithMinimalDetails() throws Exception {
        User user = new User();
        user.setUsername("u12");
        user.setEmail("u1@example.com");
        user.setPassword("Pass123!");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isOk())
                .andExpect(content().string("User registered successfully!"));
    }

    @Test
    @DisplayName("TC_POS_004: Allow case-insensitive usernames")
    void testAllowCaseInsensitiveUsernames() throws Exception {
        User user = new User();
        user.setUsername("TestUser");
        user.setEmail("testuser@example.com");
        user.setPassword("Password@123");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isOk())
                .andExpect(content().string("User registered successfully!"));
    }

    // Negative Test Cases
    @Test
    @DisplayName("TC_NEG_001: Missing username field")
    void testMissingUsernameField() throws Exception {
        String payload = "{\"email\": \"missinguser@example.com\", \"password\": \"Password@123\"}";

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.username").value("Username is required."));
    }

    @Test
    @DisplayName("TC_NEG_002: Missing email field")
    void testMissingEmailField() throws Exception {
        String payload = "{\"username\": \"nouser\", \"password\": \"Password@123\"}";

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.email").value("Email is required."));
    }

    @Test
    @DisplayName("TC_NEG_003: Missing password field")
    void testMissingPasswordField() throws Exception {
        String payload = "{\"username\": \"nopassword\", \"email\": \"nopassword@example.com\"}";

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.password").value("Password is required."));
    }

    @Test
    @DisplayName("TC_NEG_004: Duplicate username")
    void testDuplicateUsername() throws Exception {
        User user = new User();
        user.setUsername("newuser");
        user.setEmail("newemail1@example.com");
        user.setPassword("Password@123");
        user.getRoles().add("USER");
        userRepository.save(user);

        String payload = "{\"username\": \"newuser\", \"email\": \"newemail@example.com\", \"password\": \"Password@123\"}";

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
                .andExpect(status().isConflict())
                .andExpect(content().string("Username is already taken."));
    }

    @Test
    @DisplayName("TC_NEG_005: Duplicate email")
    void testDuplicateEmail() throws Exception {
        User user = new User();
        user.setUsername("newuser");
        user.setEmail("newemail@example.com");
        user.setPassword("Password@123");
        user.getRoles().add("USER");
        userRepository.save(user);

        String payload = "{\"username\": \"newuser1\", \"email\": \"newemail@example.com\", \"password\": \"Password@123\"}";

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
                .andExpect(status().isConflict())
                .andExpect(content().string("Email is already in use."));
    }

    @Test
    @DisplayName("TC_NEG_006: Weak password")
    void testWeakPassword() throws Exception {
        String payload = "{\"username\": \"weakuser\", \"email\": \"weak@example.com\", \"password\": \"123\"}";

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.password").value("Password must be at least 8 characters long."));
    }

    @Test
    @DisplayName("TC_NEG_007: Invalid email format")
    void testInvalidEmailFormat() throws Exception {
        String payload = "{\"username\": \"invalidemail\", \"email\": \"invalid-email\", \"password\": \"Password@123\"}";

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.email").value("Invalid email format."));
    }

    @Test
    @DisplayName("TC_NEG_008: Username exceeds length limit")
    void testUsernameExceedsLengthLimit() throws Exception {
        String payload = "{\"username\": \"averylongusernamethatexceedsthelimit\", \"email\": \"longuser@example.com\", \"password\": \"Password@123\"}";

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.username").value("Username must be between 3 and 20 characters."));
    }

    // Edge and Corner Cases
    @Test
    @DisplayName("TC_EDGE_001: Username with minimal length")
    void testUsernameWithMinimalLength() throws Exception {
        String payload = "{\"username\": \"u\", \"email\": \"short@example.com\", \"password\": \"Password@123\"}";

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("TC_CORNER_001: Extra fields in the payload")
    void testExtraFieldsInPayload() throws Exception {
        String payload = "{\"username\": \"extrauser\", \"email\": \"extra@example.com\", \"password\": \"Password@123\", \"extraField\": \"extradata\"}";

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
                .andExpect(status().isBadRequest());
    }

    
}
