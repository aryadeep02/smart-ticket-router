package com.aryadeep.backend.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.aryadeep.backend.entity.AuthProvider;
import com.aryadeep.backend.entity.Role;
import com.aryadeep.backend.entity.User;
import com.aryadeep.backend.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class PasswordResetServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmailService emailService;

    private PasswordEncoder passwordEncoder;
    private PasswordResetService passwordResetService;

    @BeforeEach
    void setUp() {

        passwordEncoder = new BCryptPasswordEncoder();

        passwordResetService = new PasswordResetService(
                userRepository,
                emailService,
                passwordEncoder
        );
    }
    @Test
    void shouldGenerateResetTokenAndSendEmail() {
    
        User user = createLocalUser();
    
        when(userRepository.findByEmail(user.getEmail()))
                .thenReturn(Optional.of(user));
    
        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    
        passwordResetService.requestPasswordReset(
                user.getEmail()
        );
    
        ArgumentCaptor<String> tokenCaptor =
                ArgumentCaptor.forClass(String.class);
    
        verify(emailService).sendPasswordResetEmail(
                eq(user.getEmail()),
                eq(user.getName()),
                tokenCaptor.capture()
        );
    
        String rawToken = tokenCaptor.getValue();
    
        assertEquals(
                hashToken(rawToken),
                user.getPasswordResetTokenHash()
        );
    
        verify(userRepository).save(user);
    }
    
    @Test
    void shouldNotRevealWhetherUnknownEmailExists() {

        String email = "does-not-exist@test.com";

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.empty());

        passwordResetService.requestPasswordReset(email);

        verify(userRepository, never())
                .save(any(User.class));

        verify(emailService, never())
                .sendPasswordResetEmail(
                        any(String.class),
                        any(String.class),
                        any(String.class)
                );
    }

    @Test
    void shouldIgnoreGoogleAccount() {

        User user = createLocalUser();
        user.setAuthProvider(AuthProvider.GOOGLE);

        when(userRepository.findByEmail(user.getEmail()))
                .thenReturn(Optional.of(user));

        passwordResetService.requestPasswordReset(
                user.getEmail()
        );

        verify(userRepository, never())
                .save(any(User.class));

        verify(emailService, never())
                .sendPasswordResetEmail(
                        any(String.class),
                        any(String.class),
                        any(String.class)
                );
    }

    @Test
    void shouldResetPasswordWithValidToken() {

        User user = createLocalUser();

        String rawToken = "test-reset-token-123";

        user.setPasswordResetTokenHash(
                hashToken(rawToken)
        );

        user.setPasswordResetTokenExpiry(
                LocalDateTime.now().plusMinutes(30)
        );

        when(userRepository.findByPasswordResetTokenHash(
                hashToken(rawToken)))
                .thenReturn(Optional.of(user));

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        String oldPassword = user.getPassword();

        passwordResetService.resetPassword(
                rawToken,
                "NewPassword@123"
        );

        verify(userRepository).save(user);

        org.junit.jupiter.api.Assertions.assertNotEquals(
                oldPassword,
                user.getPassword()
        );

        assertNull(user.getPasswordResetTokenHash());
        assertNull(user.getPasswordResetTokenExpiry());

        org.junit.jupiter.api.Assertions.assertTrue(
                passwordEncoder.matches(
                        "NewPassword@123",
                        user.getPassword()
                )
        );
    }

    @Test
    void shouldRejectInvalidToken() {

        when(userRepository.findByPasswordResetTokenHash(
                any(String.class)))
                .thenReturn(Optional.empty());

        IllegalStateException exception =
                assertThrows(
                        IllegalStateException.class,
                        () -> passwordResetService.resetPassword(
                                "invalid-token",
                                "NewPassword@123"
                        )
                );

        assertEquals(
                "Invalid password reset token",
                exception.getMessage()
        );
    }

    @Test
    void shouldRejectExpiredToken() {

        User user = createLocalUser();

        String rawToken = "expired-reset-token";

        user.setPasswordResetTokenHash(
                hashToken(rawToken)
        );

        user.setPasswordResetTokenExpiry(
                LocalDateTime.now().minusMinutes(1)
        );

        when(userRepository.findByPasswordResetTokenHash(
                hashToken(rawToken)))
                .thenReturn(Optional.of(user));

        IllegalStateException exception =
                assertThrows(
                        IllegalStateException.class,
                        () -> passwordResetService.resetPassword(
                                rawToken,
                                "NewPassword@123"
                        )
                );

        assertEquals(
                "Password reset token has expired",
                exception.getMessage()
        );

        assertNull(user.getPasswordResetTokenHash());
        assertNull(user.getPasswordResetTokenExpiry());

        verify(userRepository).save(user);
    }

    @Test
    void shouldRejectBlankToken() {

        IllegalStateException exception =
                assertThrows(
                        IllegalStateException.class,
                        () -> passwordResetService.resetPassword(
                                "   ",
                                "NewPassword@123"
                        )
                );

        assertEquals(
                "Password reset token is required",
                exception.getMessage()
        );
    }

    @Test
    void shouldMakeResetTokenSingleUse() {

        User user = createLocalUser();

        String rawToken = "single-use-token";

        user.setPasswordResetTokenHash(
                hashToken(rawToken)
        );

        user.setPasswordResetTokenExpiry(
                LocalDateTime.now().plusMinutes(30)
        );

        when(userRepository.findByPasswordResetTokenHash(
                hashToken(rawToken)))
                .thenReturn(Optional.of(user));

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        passwordResetService.resetPassword(
                rawToken,
                "NewPassword@123"
        );

        /*
         * Token must be removed after successful reset.
         */
        assertNull(user.getPasswordResetTokenHash());
        assertNull(user.getPasswordResetTokenExpiry());

        verify(userRepository).save(user);
    }

    private User createLocalUser() {

        return User.builder()
                .name("Test User")
                .email("test-" + System.nanoTime() + "@test.com")
                .password(
                        passwordEncoder.encode(
                                "OldPassword@123"
                        )
                )
                .role(Role.CUSTOMER)
                .authProvider(AuthProvider.LOCAL)
                .emailVerified(true)
                .build();
    }

    private String extractResetToken(User user) {

        /*
         * The service sends the raw token through EmailService.
         * Capture it from the Mockito invocation.
         */
        ArgumentCaptor<String> tokenCaptor =
                ArgumentCaptor.forClass(String.class);

        verify(emailService).sendPasswordResetEmail(
                eq(user.getEmail()),
                eq(user.getName()),
                tokenCaptor.capture()
        );

        return tokenCaptor.getValue();
    }

    private String hashToken(String token) {

        try {

            MessageDigest digest =
                    MessageDigest.getInstance("SHA-256");

            byte[] hash =
                    digest.digest(
                            token.getBytes(StandardCharsets.UTF_8)
                    );

            StringBuilder hex =
                    new StringBuilder();

            for (byte value : hash) {
                hex.append(
                        String.format("%02x", value)
                );
            }

            return hex.toString();

        } catch (Exception exception) {

            throw new IllegalStateException(
                    "Could not hash test token",
                    exception
            );
        }
    }
}