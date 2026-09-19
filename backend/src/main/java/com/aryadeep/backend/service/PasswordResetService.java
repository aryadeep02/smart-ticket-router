package com.aryadeep.backend.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.aryadeep.backend.entity.AuthProvider;
import com.aryadeep.backend.entity.User;
import com.aryadeep.backend.repository.UserRepository;

@Service
public class PasswordResetService {

    private static final int TOKEN_BYTES = 32;
    private static final int EXPIRY_HOURS = 1;

    private final UserRepository userRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final SecureRandom secureRandom;

    public PasswordResetService(
            UserRepository userRepository,
            EmailService emailService,
            PasswordEncoder passwordEncoder) {

        this.userRepository = userRepository;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
        this.secureRandom = new SecureRandom();
    }

    public void requestPasswordReset(String email) {

        User user = userRepository
                .findByEmail(email)
                .orElse(null);

        /*
         * Never reveal whether an account exists.
         */
        if (user == null) {
            return;
        }
        

        /*
         * Google accounts do not have a local password.
         */
        if (user.getAuthProvider() != AuthProvider.LOCAL) {
            return;
        }

        String rawToken = generateToken();

        String tokenHash = hashToken(rawToken);

        user.setPasswordResetTokenHash(tokenHash);
        user.setPasswordResetTokenExpiry(
                LocalDateTime.now().plusHours(EXPIRY_HOURS)
        );

        userRepository.save(user);

        emailService.sendPasswordResetEmail(
                user.getEmail(),
                user.getName(),
                rawToken
        );
    }

    public void resetPassword(
            String rawToken,
            String newPassword) {

        if (rawToken == null || rawToken.isBlank()) {
            throw new IllegalStateException(
                    "Password reset token is required"
            );
        }

        String tokenHash = hashToken(rawToken);

        User user = userRepository
                .findByPasswordResetTokenHash(tokenHash)
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Invalid password reset token"
                        ));

        LocalDateTime expiry =
                user.getPasswordResetTokenExpiry();

        if (expiry == null
                || expiry.isBefore(LocalDateTime.now())) {

            user.setPasswordResetTokenHash(null);
            user.setPasswordResetTokenExpiry(null);
            userRepository.save(user);

            throw new IllegalStateException(
                    "Password reset token has expired"
            );
        }

        /*
         * Set the new password.
         */
        user.setPassword(
                passwordEncoder.encode(newPassword)
        );

        /*
         * Make the reset token single-use.
         */
        user.setPasswordResetTokenHash(null);
        user.setPasswordResetTokenExpiry(null);

        userRepository.save(user);
    }

    private String generateToken() {

        byte[] randomBytes =
                new byte[TOKEN_BYTES];

        secureRandom.nextBytes(randomBytes);

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(randomBytes);
    }

    private String hashToken(String token) {

        try {

            MessageDigest digest =
                    MessageDigest.getInstance("SHA-256");

            byte[] hash = digest.digest(
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

        } catch (NoSuchAlgorithmException exception) {

            throw new IllegalStateException(
                    "SHA-256 algorithm not available",
                    exception
            );
        }
    }
}