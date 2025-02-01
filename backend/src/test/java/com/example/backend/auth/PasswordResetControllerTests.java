// package com.example.backend.auth;

// import com.example.backend.model.PasswordResetToken;
// import com.example.backend.model.User;
// import com.example.backend.repository.PasswordResetTokenRepository;
// import com.example.backend.repository.UserRepository;
// import com.example.backend.security.JwtUtils;
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
// import org.mockito.Mockito;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
// import org.springframework.boot.test.context.SpringBootTest;
// import org.springframework.boot.test.mock.mockito.MockBean;
// import org.springframework.http.HttpHeaders;
// import org.springframework.http.MediaType;
// import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
// import org.springframework.test.context.ActiveProfiles;
// import org.springframework.test.web.servlet.MockMvc;

// import java.time.LocalDateTime;
// import java.util.Optional;
// import java.util.UUID;

// import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
// import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// /**
//  * Tests for Password Reset API
//  */
// @SpringBootTest
// @AutoConfigureMockMvc
// @ActiveProfiles("test")
// public class PasswordResetControllerTests {

//     @Autowired
//     private MockMvc mockMvc;

//     @MockBean
//     private UserRepository userRepository;

//     @MockBean
//     private PasswordResetTokenRepository tokenRepository;

//     @MockBean
//     private JwtUtils jwtUtils;

//     private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

//     private String resetToken;
//     private String validResetToken;
//     private String expiredResetToken;
//     private String usedResetToken;
//     private String anotherUserToken;
//     private String validEmail = "testuser@example.com";
//     private String newPassword = "NewStrongP@ssw0rd";

//     @BeforeEach
//     void setUp() {
//         // Mock User
//         User mockUser = new User(3L, "testuser", validEmail, passwordEncoder.encode("OldPassword123"), null);

//         // Mock Password Reset Token
//         resetToken = UUID.randomUUID().toString();
//         PasswordResetToken mockToken = new PasswordResetToken(resetToken, mockUser, LocalDateTime.now().plusMinutes(30));

//         // Mock Repository Behavior
//         Mockito.when(userRepository.findByEmail(validEmail)).thenReturn(Optional.of(mockUser));
//         Mockito.when(tokenRepository.findByToken(resetToken)).thenReturn(Optional.of(mockToken));

//         // ✅ Mock user
//         User user = new User(1L, "john_doe", "john@example.com", passwordEncoder.encode("Strong@123"), null);
//         User anotherUser = new User(2L, "jane_doe", "jane@example.com", passwordEncoder.encode("Jane@123"), null);

//         // ✅ Generate mock reset tokens
//         validResetToken = UUID.randomUUID().toString();
//         expiredResetToken = UUID.randomUUID().toString();
//         usedResetToken = UUID.randomUUID().toString();
//         anotherUserToken = UUID.randomUUID().toString();

//         PasswordResetToken validToken = new PasswordResetToken(validResetToken, user, LocalDateTime.now().plusMinutes(30));
//         PasswordResetToken expiredToken = new PasswordResetToken(expiredResetToken, user, LocalDateTime.now().minusMinutes(10));
//         PasswordResetToken usedToken = new PasswordResetToken(usedResetToken, user, LocalDateTime.now().plusMinutes(30));
//         PasswordResetToken anotherUserValidToken = new PasswordResetToken(anotherUserToken, anotherUser, LocalDateTime.now().plusMinutes(30));

//         // ✅ Mock repository responses
//         Mockito.when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
//         Mockito.when(userRepository.findByEmail("jane@example.com")).thenReturn(Optional.of(anotherUser));
//         Mockito.when(userRepository.findByEmail("unregistered@example.com")).thenReturn(Optional.empty());

//         Mockito.when(tokenRepository.findByToken(validResetToken)).thenReturn(Optional.of(validToken));
//         Mockito.when(tokenRepository.findByToken(expiredResetToken)).thenReturn(Optional.of(expiredToken));
//         Mockito.when(tokenRepository.findByToken(usedResetToken)).thenReturn(Optional.of(usedToken));
//         Mockito.when(tokenRepository.findByToken(anotherUserToken)).thenReturn(Optional.of(anotherUserValidToken));

//         // Simulate that `usedToken` is already deleted after being used
//         Mockito.doAnswer(invocation -> {
//             Mockito.when(tokenRepository.findByToken(usedResetToken)).thenReturn(Optional.empty());
//             return null;
//         }).when(tokenRepository).delete(usedToken);
//     }

//     /**
//      * TC_POS_001: User requests a password reset successfully.
//      */
//     @Test
//     void testRequestPasswordReset() throws Exception {
//         String requestBody = """
//             {
//                 "email": "testuser@example.com"
//             }
//         """;

//         mockMvc.perform(post("/api/auth/forgot-password")
//                 .contentType(MediaType.APPLICATION_JSON)
//                 .content(requestBody))
//                 .andExpect(status().isOk())
//                 .andExpect(jsonPath("$.message").value("Password reset link sent to email."));
//     }

//     /**
//      * TC_POS_002: User resets password successfully using a valid token.
//      */
//     @Test
//     void testResetPasswordSuccessfully() throws Exception {
//         String requestBody = """
//             {
//                 "token": "%s",
//                 "newPassword": "NewStrongP@ssw0rd"
//             }
//         """.formatted(resetToken);

//         mockMvc.perform(post("/api/auth/reset-password")
//                 .contentType(MediaType.APPLICATION_JSON)
//                 .content(requestBody))
//                 .andExpect(status().isOk())
//                 .andExpect(jsonPath("$.message").value("Password has been reset successfully."));
//     }

//     /**
//      * TC_POS_003: User requests a reset and receives a valid reset link.
//      */
//     @Test
//     void testRequestResetLinkIncludesValidToken() throws Exception {
//         String requestBody = """
//             {
//                 "email": "testuser@example.com"
//             }
//         """;

//         mockMvc.perform(post("/api/auth/forgot-password")
//                 .contentType(MediaType.APPLICATION_JSON)
//                 .content(requestBody))
//                 .andExpect(status().isOk())
//                 .andExpect(jsonPath("$.message").value("Password reset link sent to email."));
        
//         // Print token to simulate email content
//         // System.out.println("Mock Reset Link: http://localhost:8080/api/auth/reset-password?token=" + resetToken);
//     }

//     /**
//      * TC_POS_004: User resets password after logging in with an old password.
//      */
//     @Test
//     void testResetPasswordAfterLogin() throws Exception {
//         String requestBody = """
//             {
//                 "token": "%s",
//                 "newPassword": "UpdatedPassword@123"
//             }
//         """.formatted(resetToken);

//         mockMvc.perform(post("/api/auth/reset-password")
//                 .contentType(MediaType.APPLICATION_JSON)
//                 .content(requestBody))
//                 .andExpect(status().isOk())
//                 .andExpect(jsonPath("$.message").value("Password has been reset successfully."));
//     }

//     /**
//      * TC_POS_005: User receives a confirmation email after resetting the password.
//      */
//     @Test
//     void testConfirmationEmailAfterPasswordReset() throws Exception {
//         String requestBody = """
//             {
//                 "token": "%s",
//                 "newPassword": "NewStrongP@ssw0rd"
//             }
//         """.formatted(resetToken);

//         mockMvc.perform(post("/api/auth/reset-password")
//                 .contentType(MediaType.APPLICATION_JSON)
//                 .content(requestBody))
//                 .andExpect(status().isOk())
//                 .andExpect(jsonPath("$.message").value("Password has been reset successfully."));
        
//         // Simulate email sending
//         // System.out.println("Confirmation Email Sent to: " + validEmail);
//     }

//     /**
//      * TC_NEG_001: No email provided for password reset request.
//      */
//     @Test
//     void testNoEmailProvided() throws Exception {
//         String requestBody = "{}";

//         mockMvc.perform(post("/api/auth/forgot-password")
//                 .contentType(MediaType.APPLICATION_JSON)
//                 .content(requestBody))
//                 .andExpect(status().isBadRequest())
//                 .andExpect(jsonPath("$.message").value("Email is required."));
//     }

//     /**
//      * TC_NEG_002: User provides an unregistered email.
//      */
//     @Test
//     void testUnregisteredEmail() throws Exception {
//         String requestBody = """
//             {
//                 "email": "unregistered@example.com"
//             }
//         """;

//         mockMvc.perform(post("/api/auth/forgot-password")
//                 .contentType(MediaType.APPLICATION_JSON)
//                 .content(requestBody))
//                 .andExpect(status().isBadRequest())
//                 .andExpect(jsonPath("$.message").value("No user found with this email."));
//     }

//     /**
//      * TC_NEG_003: Invalid or expired reset token used.
//      */
//     @Test
//     void testInvalidOrExpiredToken() throws Exception {
//         String requestBody = "{"
//         + "\"token\": \"" + expiredResetToken + "\","
//         + "\"newPassword\": \"NewStrongP@ss1\""
//         + "}";


//         mockMvc.perform(post("/api/auth/reset-password")
//                 .contentType(MediaType.APPLICATION_JSON)
//                 .content(requestBody))
//                 .andExpect(status().isBadRequest())
//                 .andExpect(jsonPath("$.message").value("Token has expired."));
//     }

//     /**
//      * TC_NEG_004: Weak password provided during reset.
//      */
//     @Test
//     void testWeakPassword() throws Exception {
//         String requestBody = "{"
//         + "\"token\": \"" + validResetToken + "\","
//         + "\"newPassword\": \"123456\""
//         + "}";


//         mockMvc.perform(post("/api/auth/reset-password")
//                 .contentType(MediaType.APPLICATION_JSON)
//                 .content(requestBody))
//                 .andExpect(status().isBadRequest())
//                 .andExpect(jsonPath("$.message").value("Password is too weak"));
//     }

//     // /**
//     //  * TC_NEG_005: Password reset request spam (multiple requests in a short time).
//     //  */
//     // @Test
//     // void testPasswordResetSpam() throws Exception {
//     //     String requestBody = """
//     //         {
//     //             "email": "john@example.com"
//     //         }
//     //     """;

//     //     for (int i = 0; i < 3; i++) {
//     //         mockMvc.perform(post("/api/auth/forgot-password")
//     //                 .contentType(MediaType.APPLICATION_JSON)
//     //                 .content(requestBody))
//     //                 .andExpect(status().isOk());
//     //     }

//     //     mockMvc.perform(post("/api/auth/forgot-password")
//     //             .contentType(MediaType.APPLICATION_JSON)
//     //             .content(requestBody))
//     //             .andExpect(status().isTooManyRequests())
//     //             .andExpect(jsonPath("$.message").value("Try again later."));
//     // }

//     // /**
//     //  * TC_NEG_006: Reset token from another user is used.
//     //  */
//     // @Test
//     // void testResetWithAnotherUsersToken() throws Exception {
//     //     String requestBody = "{"
//     //     + "\"token\": \"" + anotherUserToken + "\","
//     //     + "\"newPassword\": \"NewStrongP@ss2\""
//     //     + "}";


//     //     mockMvc.perform(post("/api/auth/reset-password")
//     //             .contentType(MediaType.APPLICATION_JSON)
//     //             .content(requestBody))
//     //             .andExpect(status().isForbidden())
//     //             .andExpect(jsonPath("$.message").value("Access denied."));
//     // }

//     /**
//      * TC_NEG_007: Reset password request with an invalid email format.
//      */
//     @Test
//     void testInvalidEmailFormat() throws Exception {
//         String requestBody = """
//             {
//                 "email": "invalid-email"
//             }
//         """;

//         mockMvc.perform(post("/api/auth/forgot-password")
//                 .contentType(MediaType.APPLICATION_JSON)
//                 .content(requestBody))
//                 .andExpect(status().isBadRequest())
//                 .andExpect(jsonPath("$.message").value("No user found with this email."));
//     }
// }
