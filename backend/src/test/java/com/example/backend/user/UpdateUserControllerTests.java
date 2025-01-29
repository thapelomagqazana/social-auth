package com.example.backend.user;

import com.example.backend.model.User;
import com.example.backend.repository.UserRepository;
import com.example.backend.security.JwtUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.util.Optional;
import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests for PUT /api/users/{id} - Update a specific user's profile.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class UpdateUserControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtUtils jwtUtils;

    @MockBean
    private UserRepository userRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private String adminToken;
    private String userToken;

    @BeforeEach
    void setUp() {
        // Mock admin user
        User adminUser = new User(1L, "adminUser", "admin@example.com", passwordEncoder.encode("Admin@123"), Set.of("ADMIN"));

        // Mock normal user
        User normalUser = new User(5L, "normalUser", "user@example.com", passwordEncoder.encode("User@123"), Set.of("USER"));

        // Mock another user
        User anotherUser = new User(10L, "anotherUser", "another@example.com", passwordEncoder.encode("Another@123"), Set.of("USER"));

        // ✅ Mock a user with multiple roles
        User multiRoleUser = new User(8L, "multiRoleUser", "multi@example.com", passwordEncoder.encode("Password@123"), Set.of("USER"));

        // ✅ Mock a user with multiple fields to update
        User complexUser = new User(6L, "oldUsername", "oldemail@example.com", passwordEncoder.encode("OldPassword@123"), Set.of("USER"));

        // ✅ Generate JWT tokens
        adminToken = jwtUtils.generateToken(adminUser.getUsername(), 86400000, "ADMIN");
        userToken = jwtUtils.generateToken(normalUser.getUsername(), 86400000, "USER");

        // ✅ Mock user repository behavior
        Mockito.when(userRepository.findById(5L)).thenReturn(Optional.of(normalUser));
        Mockito.when(userRepository.findById(10L)).thenReturn(Optional.of(anotherUser));
        Mockito.when(userRepository.findById(8L)).thenReturn(Optional.of(multiRoleUser));
        Mockito.when(userRepository.findById(6L)).thenReturn(Optional.of(complexUser));
    }

    /**
     * ✅ TC_POS_001: Update own profile (username, email) as a standard user.
     */
    @Test
    void testUpdateOwnProfileAsUser() throws Exception {
        String requestBody = objectMapper.writeValueAsString(
                new User(null, "updatedUser", "updated@example.com", null, null)
        );

        mockMvc.perform(put("/api/users/5")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("updatedUser"))
                .andExpect(jsonPath("$.email").value("updated@example.com"));
    }

    /**
     * ✅ TC_POS_002: Update another user's profile as an admin.
     */
    @Test
    void testAdminUpdatesAnotherUserProfile() throws Exception {
        String requestBody = objectMapper.writeValueAsString(
                new User(null, "adminUpdated", "adminUpdated@example.com", null, null)
        );

        mockMvc.perform(put("/api/users/10")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("adminUpdated"))
                .andExpect(jsonPath("$.email").value("adminUpdated@example.com"));
    }

    /**
     * ✅ TC_POS_003: Update password as a standard user.
     */
    @Test
    void testUpdatePasswordAsUser() throws Exception {
        String requestBody = objectMapper.writeValueAsString(
                new User(null, null, null, "NewPassword@123", null)
        );

        mockMvc.perform(put("/api/users/5")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Password updated successfully"));
    }

    /**
     * ✅ TC_POS_004: Update roles as an admin.
     */
    @Test
    void testUpdateRolesAsAdmin() throws Exception {
        String requestBody = objectMapper.writeValueAsString(
                new User(null, null, null, null, Set.of("ADMIN"))
        );

        mockMvc.perform(put("/api/users/8")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roles").isArray())
                .andExpect(jsonPath("$.roles.length()").value(1))
                .andExpect(jsonPath("$.roles[0]").value("ADMIN"));
    }

    /**
     * ✅ TC_POS_005: Update multiple fields at once.
     */
    @Test
    void testUpdateMultipleFieldsAtOnce() throws Exception {
        String requestBody = objectMapper.writeValueAsString(
                new User(null, "newUsername", "newemail@example.com", "NewStrongPassword@123", Set.of("USER", "ADMIN"))
        );

        mockMvc.perform(put("/api/users/6")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("newUsername"))
                .andExpect(jsonPath("$.email").value("newemail@example.com"))
                .andExpect(jsonPath("$.roles").isArray())
                .andExpect(jsonPath("$.roles.length()").value(2))
                .andExpect(jsonPath("$.roles[0]").value("USER"))
                .andExpect(jsonPath("$.roles[1]").value("ADMIN"));
    }
}
