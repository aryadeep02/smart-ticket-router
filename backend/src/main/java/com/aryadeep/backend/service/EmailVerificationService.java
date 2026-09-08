package com.aryadeep.backend.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;

import org.springframework.stereotype.Service;

import com.aryadeep.backend.entity.User;
import com.aryadeep.backend.repository.UserRepository;

@Service
public class EmailVerificationService {

    private static final int TOKEN_BYTES = 32;
    private static final int EXPIRY_HOURS = 24;

    private final UserRepository userRepository;
    private final SecureRandom secureRandom;

    public EmailVerificationService(
            UserRepository userRepository) {

        this.userRepository = userRepository;
        this.secureRandom = new SecureRandom();
    }

    public String generateVerificationToken(User user) {

        byte[] randomBytes = new byte[TOKEN_BYTES];
        secureRandom.nextBytes(randomBytes);

        String rawToken = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(randomBytes);

        String tokenHash = hashToken(rawToken);

        user.setVerificationTokenHash(tokenHash);
        user.setVerificationTokenExpiry(
                LocalDateTime.now().plusHours(EXPIRY_HOURS));

        userRepository.save(user);

        return rawToken;
    }

    public void verifyEmail(String rawToken) {

        if (rawToken == null || rawToken.isBlank()) {
            throw new IllegalStateException(
                    "Verification token is required");
        }

        String tokenHash = hashToken(rawToken);

        User user = userRepository
                .findByVerificationTokenHash(tokenHash)
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Invalid verification token"));

        if (user.isEmailVerified()) {
            throw new IllegalStateException(
                    "Email is already verified");
        }

        LocalDateTime expiry =
                user.getVerificationTokenExpiry();

        if (expiry == null ||
                expiry.isBefore(LocalDateTime.now())) {

            throw new IllegalStateException(
                    "Verification token has expired");
        }

        user.setEmailVerified(true);
        user.setVerificationTokenHash(null);
        user.setVerificationTokenExpiry(null);

        userRepository.save(user);
    }

    private String hashToken(String token) {

        try {
            MessageDigest digest =
                    MessageDigest.getInstance("SHA-256");

            byte[] hash = digest.digest(
                    token.getBytes(StandardCharsets.UTF_8));

            StringBuilder hex = new StringBuilder();

            for (byte value : hash) {
                hex.append(String.format("%02x", value));
            }

            return hex.toString();

        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(
                    "SHA-256 algorithm not available",
                    exception);
        }
    }
}