package com.aryadeep.backend.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.aryadeep.backend.dto.AdminUserResponse;
import com.aryadeep.backend.dto.LoginResponse;
import com.aryadeep.backend.dto.RegisterResponse;
import com.aryadeep.backend.entity.AuthProvider;
import com.aryadeep.backend.entity.Role;
import com.aryadeep.backend.entity.SupportTeam;
import com.aryadeep.backend.entity.User;
import com.aryadeep.backend.exception.EmailAlreadyExistsException;
import com.aryadeep.backend.exception.InvalidCredentialsException;
import com.aryadeep.backend.repository.SupportTeamRepository;
import com.aryadeep.backend.repository.UserRepository;

@Service
public class UserService {

    private static final Logger logger =
            LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final SupportTeamRepository supportTeamRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final EmailVerificationService emailVerificationService;
    private final EmailService emailService;

    public UserService(
            UserRepository userRepository,
            SupportTeamRepository supportTeamRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            EmailVerificationService emailVerificationService,
            EmailService emailService) {

        this.userRepository = userRepository;
        this.supportTeamRepository = supportTeamRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.emailVerificationService = emailVerificationService;
        this.emailService = emailService;
    }

    public RegisterResponse registerUser(
            String name,
            String email,
            String password) {

        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyExistsException(
                    "Email already registered");
        }

        User user = User.builder()
                .name(name)
                .email(email)
                .password(passwordEncoder.encode(password))
                .role(Role.CUSTOMER)
                .authProvider(AuthProvider.LOCAL)
                .emailVerified(false)
                .build();

        User savedUser = userRepository.save(user);

        String verificationToken =
                emailVerificationService.generateVerificationToken(
                        savedUser);

        boolean verificationEmailSent = true;

        try {

            emailService.sendVerificationEmail(
                    savedUser.getEmail(),
                    savedUser.getName(),
                    verificationToken);

        } catch (MailException exception) {

            logger.error(
                    "Failed to send verification email to {}: {}",
                    savedUser.getEmail(),
                    exception.getMessage());

            verificationEmailSent = false;
        }

        String message;

        if (verificationEmailSent) {

            message =
                    "Account created. Please check your email "
                            + "to verify your account.";

        } else {

            message =
                    "Account created, but the verification email "
                            + "could not be sent. Please request "
                            + "a new verification email.";
        }

        return new RegisterResponse(
                savedUser.getId(),
                savedUser.getName(),
                savedUser.getEmail(),
                savedUser.getRole().name(),
                savedUser.isEmailVerified(),
                verificationEmailSent,
                message);
    }

    public User createUserWithRole(
            String name,
            String email,
            String password,
            Role role,
            Long supportTeamId) {

        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyExistsException(
                    "Email already registered");
        }

        SupportTeam supportTeam = null;

        if (role == Role.AGENT) {

            if (supportTeamId == null) {
                throw new IllegalStateException(
                        "Support team is required for an AGENT");
            }

            supportTeam = supportTeamRepository
                    .findById(supportTeamId)
                    .orElseThrow(() ->
                            new RuntimeException(
                                    "Support team not found"));
        }

        User user = User.builder()
                .name(name)
                .email(email)
                .password(passwordEncoder.encode(password))
                .role(role)
                .authProvider(AuthProvider.LOCAL)
                .emailVerified(true)
                .supportTeam(supportTeam)
                .build();

        return userRepository.save(user);
    }

    public LoginResponse loginUser(
            String email,
            String password) {

        User user = userRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new InvalidCredentialsException(
                                "Invalid email or password"));

        if (user.getPassword() == null ||
                !passwordEncoder.matches(
                        password,
                        user.getPassword())) {

            throw new InvalidCredentialsException(
                    "Invalid email or password");
        }

        if (user.getAuthProvider() == AuthProvider.LOCAL &&
                !user.isEmailVerified()) {

            throw new IllegalStateException(
                    "Please verify your email before logging in");
        }

        String token = jwtService.generateToken(
                user.getEmail());

        return new LoginResponse(
                token,
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole().name());
    }

    public List<AdminUserResponse> getAllUsers() {

        return userRepository.findAll()
                .stream()
                .map(user -> new AdminUserResponse(
                        user.getId(),
                        user.getName(),
                        user.getEmail(),
                        user.getRole().name(),
                        user.getSupportTeam() != null
                                ? user.getSupportTeam().getId()
                                : null,
                        user.getSupportTeam() != null
                                ? user.getSupportTeam().getName()
                                : null))
                .toList();
    }

    public User findOrCreateGoogleUser(
            String name,
            String email) {

        return userRepository.findByEmail(email)
                .orElseGet(() -> {

                    User user = User.builder()
                            .name(name)
                            .email(email)
                            .password(null)
                            .role(Role.CUSTOMER)
                            .authProvider(AuthProvider.GOOGLE)
                            .emailVerified(true)
                            .build();

                    return userRepository.save(user);
                });
    }

    public LoginResponse getCurrentUser(String email) {

        User user = userRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new InvalidCredentialsException(
                                "User not found"));

        return new LoginResponse(
                "",
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole().name());
    }

    public void resendVerificationEmail(String email) {

        User user = userRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new InvalidCredentialsException(
                                "Unable to process verification request"));

        if (user.getAuthProvider() != AuthProvider.LOCAL) {
            throw new IllegalStateException(
                    "Email verification is only available for local accounts");
        }

        if (user.isEmailVerified()) {
            throw new IllegalStateException(
                    "Email is already verified");
        }

        String verificationToken =
                emailVerificationService.generateVerificationToken(user);

        try {

            emailService.sendVerificationEmail(
                    user.getEmail(),
                    user.getName(),
                    verificationToken);

        } catch (MailException exception) {

            logger.error(
                    "Failed to resend verification email to {}: {}",
                    user.getEmail(),
                    exception.getMessage());

            throw new IllegalStateException(
                    "Unable to send verification email");
        }
    }
}