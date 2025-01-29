package com.example.backend.auth;

import com.example.backend.security.JwtBlacklistService;
import com.example.backend.security.JwtUtils;
import com.example.backend.security.UserDetailsServiceImpl;

import org.junit.jupiter.api.AfterEach;
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
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.fail;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class AuthControllerLogoutTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtUtils jwtUtils;

    @MockBean(name = "userDetailsService")
    private UserDetailsService userDetailsService;

    @MockBean
    private UserDetailsServiceImpl userDetailsServiceImpl;
    
    @Autowired
    private JwtBlacklistService jwtBlacklistService;

    private String validToken;
    private String refreshToken;
    private String secondDeviceToken;

    private final String ROLE = "ROLE_USER";

    @BeforeEach
    void setUp() {
        // Clear token blacklist before each test
        jwtBlacklistService.blacklistToken("", 0);

        // Mock user data to prevent "User Not Found" error
        UserDetails mockUser = new User("validuser", "Password@123", Collections.emptyList());
        Mockito.when(userDetailsService.loadUserByUsername("validuser")).thenReturn(mockUser);

        // Generate fresh JWT tokens for the test user
        validToken = jwtUtils.generateToken("validuser", 86400000, ROLE);
        refreshToken = jwtUtils.generateToken("validuser", 86400000, ROLE);
        secondDeviceToken = jwtUtils.generateToken("validuser", 86400000, ROLE);
    }

    @AfterEach
    void tearDown() {
        // Explicitly remove the test tokens from blacklist
        jwtBlacklistService.blacklistToken(validToken, 1);  // Short expiration
        jwtBlacklistService.blacklistToken(refreshToken, 1);
        jwtBlacklistService.blacklistToken(secondDeviceToken, 1);
    }
    

    /**
     * TC_POS_001: Valid logout request.
     */
    @Test
    void testValidLogoutRequest() throws Exception {
        mockMvc.perform(post("/api/auth/logout")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("User logged out successfully."));
    }

    /**
     * TC_POS_002: Logout multiple times.
     */
    @Test
    void testLogoutMultipleTimes() throws Exception {
        String firstDeviceToken = jwtUtils.generateToken("validuser", 86400000, ROLE);
        String secondDeviceToken = jwtUtils.generateToken("validuser", 86400000, ROLE);
        // First logout attempt should succeed
        mockMvc.perform(post("/api/auth/logout")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + firstDeviceToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("User logged out successfully."));

        // Second logout attempt should return 401 Unauthorized
        mockMvc.perform(post("/api/auth/logout")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + secondDeviceToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(content().json("{\"message\": \"Invalid token.\"}"));

        // Explicitly remove the test tokens from blacklist
        jwtBlacklistService.blacklistToken(firstDeviceToken, 1);
        jwtBlacklistService.blacklistToken(secondDeviceToken, 1);
    }

    /**
     * TC_POS_003: Logout with a refresh token.
     */
    @Test
    void testLogoutWithRefreshToken() throws Exception {
        mockMvc.perform(post("/api/auth/logout")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + refreshToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("User logged out successfully."));
    }

    /**
     * TC_POS_004: Logout with a valid session.
     */
    @Test
    void testLogoutWithValidSession() throws Exception {
        // Logout request should succeed
        mockMvc.perform(post("/api/auth/logout")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("User logged out successfully."));

        // Ensure session is invalidated by trying to logout again
        mockMvc.perform(post("/api/auth/logout")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    /**
     * TC_NEG_001: No authentication token provided.
     */
    @Test
    void testNoAuthTokenProvided() throws Exception {
        mockMvc.perform(post("/api/auth/logout")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(content().json("{\"message\": \"Token is missing.\"}"));
    }

    /**
     * TC_NEG_002: Invalid authentication token.
     */
    @Test
    void testInvalidAuthToken() throws Exception {
        String invalidToken = "Invalid.JWT.Token";
        
        mockMvc.perform(post("/api/auth/logout")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + invalidToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(content().json("{\"message\": \"Invalid token.\"}"));
    }

    /**
     * TC_NEG_003: Expired token used for logout.
     */
    @Test
    void testExpiredToken() throws Exception {
        // Generate an expired token by setting expiration time to past
        String expiredToken = jwtUtils.generateToken("validuser", -3600000, ROLE); // 1 hour ago

        mockMvc.perform(post("/api/auth/logout")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + expiredToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(content().json("{\"message\": \"Invalid token.\"}"));
    }

    /**
     * TC_NEG_004: Logout after password reset.
     */
    @Test
    void testLogoutAfterPasswordReset() throws Exception {
        String oldToken = jwtUtils.generateToken("validuser", 86400000, ROLE);

        // Simulate password reset that invalidates old tokens
        jwtBlacklistService.blacklistToken(oldToken, 86400000);

        mockMvc.perform(post("/api/auth/logout")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + oldToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(content().json("{\"message\": \"Invalid token.\"}"));
    }

    // /**
    //  * TC_NEG_005: Logout with another user's token.
    //  */
    // @Test
    // void testLogoutWithAnotherUsersToken() throws Exception {
    //     String anotherUserToken = jwtUtils.generateToken("anotheruser", 86400000);

    //     mockMvc.perform(post("/api/auth/logout")
    //             .header(HttpHeaders.AUTHORIZATION, "Bearer " + anotherUserToken)
    //             .contentType(MediaType.APPLICATION_JSON))
    //             .andExpect(status().isForbidden())
    //             .andExpect(content().json("{\"message\": \"Access denied.\"}"));
    // }

    /**
     * TC_NEG_006: Logout from revoked token.
     */
    @Test
    void testLogoutWithRevokedToken() throws Exception {
        jwtBlacklistService.blacklistToken(validToken, 85400000); // Blacklist valid token

        mockMvc.perform(post("/api/auth/logout")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(content().json("{\"message\": \"Invalid token.\"}"));
    }

    /**
     * TC_NEG_007: Logout when database is down.
     */
    // @Test
    // void testLogoutWhenDatabaseIsDown() throws Exception {
    //     // Simulate DB failure by throwing an exception when UserDetailsService is called
    //     Mockito.doThrow(new RuntimeException("Database unreachable"))
    //             .when(userDetailsService).loadUserByUsername("validuser");

    //     mockMvc.perform(post("/api/auth/logout")
    //             .header(HttpHeaders.AUTHORIZATION, "Bearer " + validToken)
    //             .contentType(MediaType.APPLICATION_JSON))
    //             .andExpect(status().isInternalServerError())
    //             .andExpect(content().json("{\"message\": \"Service unavailable.\"}"));
    // }

    // /**
    //  * TC_NEG_008: Logout when Redis cache is down.
    //  */
    // @Test
    // void testLogoutWhenRedisIsDown() throws Exception {
    //     // Simulate Redis failure
    //     Mockito.doThrow(new RuntimeException("Redis unavailable"))
    //             .when(jwtBlacklistService).blacklistToken(Mockito.anyString(), Mockito.anyLong());

    //     mockMvc.perform(post("/api/auth/logout")
    //             .header(HttpHeaders.AUTHORIZATION, "Bearer " + validToken)
    //             .contentType(MediaType.APPLICATION_JSON))
    //             .andExpect(status().isInternalServerError())
    //             .andExpect(content().json("{\"message\": \"Logout failed, please try again.\"}"));
    // }

    // /**
    //  * TC_NEG_009: Logout with invalid refresh token.
    //  */
    // @Test
    // void testLogoutWithInvalidRefreshToken() throws Exception {
    //     String invalidRefreshToken = "InvalidRefreshToken123";

    //     mockMvc.perform(post("/api/auth/logout")
    //             .header(HttpHeaders.AUTHORIZATION, "Bearer " + invalidRefreshToken)
    //             .contentType(MediaType.APPLICATION_JSON))
    //             .andExpect(status().isUnauthorized())
    //             .andExpect(content().json("{\"message\": \"Invalid refresh token.\"}"));
    // }

    /**
     * TC_CORNER_001: Logout request with extra fields.
     */
    @Test
    void testLogoutWithExtraFields() throws Exception {
        mockMvc.perform(post("/api/auth/logout")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{ \"extraField\": \"unexpected\" }"))  // Extra field in JSON body
                .andExpect(status().isOk())  // Should be ignored if not required
                .andExpect(content().string("User logged out successfully."));
    }

    // /**
    //  * TC_CORNER_002: Logout request with empty JSON body.
    //  */
    // @Test
    // void testLogoutWithEmptyJsonBody() throws Exception {
    //     mockMvc.perform(post("/api/auth/logout")
    //             .header(HttpHeaders.AUTHORIZATION, "Bearer " + validToken)
    //             .contentType(MediaType.APPLICATION_JSON)
    //             .content("{}"))  // Empty JSON body
    //             .andExpect(status().isBadRequest())
    //             .andExpect(content().json("{\"message\": \"Invalid request format.\"}"));
    // }

    // /**
    //  * TC_CORNER_003: Logout request with an invalid JSON format.
    //  */
    // @Test
    // void testLogoutWithInvalidJson() throws Exception {
    //     mockMvc.perform(post("/api/auth/logout")
    //             .header(HttpHeaders.AUTHORIZATION, "Bearer " + validToken)
    //             .contentType(MediaType.APPLICATION_JSON)
    //             .content("{ \"token\": \"Bearer token..."))  // Missing closing brace
    //             .andExpect(status().isBadRequest())
    //             .andExpect(content().json("{\"message\": \"Malformed JSON.\"}"));
    // }

    // /**
    //  * TC_CORNER_004: Logout during high traffic.
    //  */
    // @Test
    // void testLogoutDuringHighTraffic() throws Exception {
    //     // Simulate multiple concurrent logout requests
    //     IntStream.range(0, 10).parallel().forEach(i -> {
    //         try {
    //             mockMvc.perform(post("/api/auth/logout")
    //                     .header(HttpHeaders.AUTHORIZATION, "Bearer " + validToken)
    //                     .contentType(MediaType.APPLICATION_JSON))
    //                     .andExpect(status().isOk())
    //                     .andExpect(content().string("User logged out successfully."));
    //         } catch (Exception e) {
    //             fail("High traffic logout test failed: " + e.getMessage());
    //         }
    //     });
    // }

    // /**
    //  * TC_CORNER_005: Logout with database inconsistency.
    //  */
    // @Test
    // void testLogoutWithDatabaseInconsistency() throws Exception {
    //     // Simulate user deletion before logout
    //     Mockito.when(userDetailsService.loadUserByUsername("validuser")).thenThrow(new UsernameNotFoundException("User no longer exists"));

    //     mockMvc.perform(post("/api/auth/logout")
    //             .header(HttpHeaders.AUTHORIZATION, "Bearer " + validToken)
    //             .contentType(MediaType.APPLICATION_JSON))
    //             .andExpect(status().isUnauthorized())
    //             .andExpect(content().json("{\"message\": \"User no longer exists.\"}"));
    // }


}
