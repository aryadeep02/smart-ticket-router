package com.aryadeep.backend;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.aryadeep.backend.entity.AuthProvider;
import com.aryadeep.backend.entity.Role;
import com.aryadeep.backend.entity.User;
import com.aryadeep.backend.repository.UserRepository;
import com.aryadeep.backend.service.EmailService;

@SpringBootTest
@AutoConfigureMockMvc
class PasswordResetIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private EmailService emailService;

    @Test
    void forgotPasswordShouldReturnSuccessForExistingUser()
            throws Exception {

        User user = createLocalUser();

        String requestBody = """
                {
                    "email": "%s"
                }
                """.formatted(user.getEmail());

        mockMvc.perform(
                post("/api/v1/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
        )
        .andExpect(status().isOk())
        .andExpect(
                jsonPath("$.message")
                        .value(
                                "If an account exists for this email, "
                                        + "a password reset link has been sent."
                        )
        );

        verify(emailService).sendPasswordResetEmail(
                eq(user.getEmail()),
                eq(user.getName()),
                anyString()
        );
    }

    @Test
    void forgotPasswordShouldReturnSameResponseForUnknownEmail()
            throws Exception {

        String email =
                "unknown-"
                        + UUID.randomUUID()
                        + "@test.com";

        String requestBody = """
                {
                    "email": "%s"
                }
                """.formatted(email);

        mockMvc.perform(
                post("/api/v1/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
        )
        .andExpect(status().isOk())
        .andExpect(
                jsonPath("$.message")
                        .value(
                                "If an account exists for this email, "
                                        + "a password reset link has been sent."
                        )
        );

        verify(emailService, never())
                .sendPasswordResetEmail(
                        anyString(),
                        anyString(),
                        anyString()
                );
    }

    @Test
    void forgotPasswordShouldRejectInvalidEmail()
            throws Exception {

        String requestBody = """
                {
                    "email": "not-an-email"
                }
                """;

        mockMvc.perform(
                post("/api/v1/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
        )
        .andExpect(status().isBadRequest());
    }

    @Test
    void forgotPasswordShouldRejectBlankEmail()
            throws Exception {

        String requestBody = """
                {
                    "email": ""
                }
                """;

        mockMvc.perform(
                post("/api/v1/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
        )
        .andExpect(status().isBadRequest());
    }

    @Test
    void resetPasswordShouldAcceptValidToken()
            throws Exception {

        User user = createLocalUser();

        String rawToken =
                "valid-reset-token-"
                        + UUID.randomUUID();

        user.setPasswordResetTokenHash(
                sha256(rawToken)
        );

        user.setPasswordResetTokenExpiry(
                LocalDateTime.now().plusHours(1)
        );

        userRepository.save(user);

        String oldPassword = "OldPassword@123";
        String newPassword = "NewPassword@456";

        String requestBody = """
                {
                    "token": "%s",
                    "newPassword": "%s"
                }
                """.formatted(
                        rawToken,
                        newPassword
                );

        mockMvc.perform(
                post("/api/v1/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
        )
        .andExpect(status().isOk())
        .andExpect(
                jsonPath("$.message")
                        .value(
                                "Password reset successfully"
                        )
        );

        User updatedUser = userRepository
                .findById(user.getId())
                .orElseThrow();

        assertTrue(
                passwordEncoder.matches(
                        newPassword,
                        updatedUser.getPassword()
                )
        );

        assertFalse(
                passwordEncoder.matches(
                        oldPassword,
                        updatedUser.getPassword()
                )
        );

        assertTrue(
                updatedUser.getPasswordResetTokenHash() == null
        );

        assertTrue(
                updatedUser.getPasswordResetTokenExpiry() == null
        );
    }

    @Test
    void resetPasswordShouldRejectInvalidToken()
            throws Exception {

        String requestBody = """
                {
                    "token": "invalid-token",
                    "newPassword": "NewPassword@456"
                }
                """;

        mockMvc.perform(
                post("/api/v1/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
        )
        .andExpect(status().isBadRequest())
        .andExpect(
                jsonPath("$.message")
                        .value(
                                "Invalid password reset token"
                        )
        );
    }

    @Test
    void resetPasswordShouldRejectExpiredToken()
            throws Exception {

        User user = createLocalUser();

        String rawToken =
                "expired-token-"
                        + UUID.randomUUID();

        user.setPasswordResetTokenHash(
                sha256(rawToken)
        );

        user.setPasswordResetTokenExpiry(
                LocalDateTime.now().minusMinutes(1)
        );

        userRepository.save(user);

        String requestBody = """
                {
                    "token": "%s",
                    "newPassword": "NewPassword@456"
                }
                """.formatted(rawToken);

        mockMvc.perform(
                post("/api/v1/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
        )
        .andExpect(status().isBadRequest())
        .andExpect(
                jsonPath("$.message")
                        .value(
                                "Password reset token has expired"
                        )
        );

        User updatedUser = userRepository
                .findById(user.getId())
                .orElseThrow();

        assertTrue(
                updatedUser.getPasswordResetTokenHash() == null
        );

        assertTrue(
                updatedUser.getPasswordResetTokenExpiry() == null
        );
    }

    @Test
    void resetPasswordShouldRejectMissingToken()
            throws Exception {

        String requestBody = """
                {
                    "token": "",
                    "newPassword": "NewPassword@456"
                }
                """;

        mockMvc.perform(
                post("/api/v1/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
        )
        .andExpect(status().isBadRequest());
    }

    @Test
    void resetPasswordShouldRejectWeakPassword()
            throws Exception {

        String requestBody = """
                {
                    "token": "some-token",
                    "newPassword": "123"
                }
                """;

        mockMvc.perform(
                post("/api/v1/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
        )
        .andExpect(status().isBadRequest());
    }

    private User createLocalUser() {

        String email =
                "reset-test-"
                        + UUID.randomUUID()
                        + "@test.com";

        User user = User.builder()
                .name("Password Reset Test User")
                .email(email)
                .password(
                        passwordEncoder.encode(
                                "OldPassword@123"
                        )
                )
                .role(Role.CUSTOMER)
                .authProvider(AuthProvider.LOCAL)
                .emailVerified(true)
                .build();

        return userRepository.save(user);
    }

    private String sha256(String value) {

        try {

            java.security.MessageDigest digest =
                    java.security.MessageDigest
                            .getInstance("SHA-256");

            byte[] hash =
                    digest.digest(
                            value.getBytes(
                                    java.nio.charset.StandardCharsets.UTF_8
                            )
                    );

            StringBuilder hex =
                    new StringBuilder();

            for (byte currentByte : hash) {

                hex.append(
                        String.format(
                                "%02x",
                                currentByte
                        )
                );
            }

            return hex.toString();

        } catch (java.security.NoSuchAlgorithmException exception) {

            throw new IllegalStateException(
                    "SHA-256 algorithm not available",
                    exception
            );
        }
    }
}