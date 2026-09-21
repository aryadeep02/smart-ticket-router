package com.aryadeep.backend.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.aryadeep.backend.dto.ForgotPasswordRequest;
import com.aryadeep.backend.dto.LoginRequest;
import com.aryadeep.backend.dto.LoginResponse;
import com.aryadeep.backend.dto.RegisterRequest;
import com.aryadeep.backend.dto.RegisterResponse;
import com.aryadeep.backend.dto.ResetPasswordRequest;
import com.aryadeep.backend.service.EmailVerificationService;
import com.aryadeep.backend.service.PasswordResetService;
import com.aryadeep.backend.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(
        name = "Authentication",
        description = "Registration, login, email verification and password recovery operations"
)
public class AuthController {

    private final UserService userService;
    private final EmailVerificationService emailVerificationService;
    private final PasswordResetService passwordResetService;

    public AuthController(
            UserService userService,
            EmailVerificationService emailVerificationService,
            PasswordResetService passwordResetService) {

        this.userService = userService;
        this.emailVerificationService =
                emailVerificationService;
        this.passwordResetService =
                passwordResetService;
    }

    @Operation(
            summary = "Register a new customer",
            description = "Creates a local customer account and sends an email verification link."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Account created successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid registration data"
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Email is already registered"
            )
    })
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public RegisterResponse register(
            @Valid @RequestBody RegisterRequest request) {

        return userService.registerUser(
                request.name(),
                request.email(),
                request.password()
        );
    }

    @Operation(
            summary = "Login",
            description = "Authenticates a local user and returns an application JWT."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Login successful"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Invalid credentials or unverified email"
            )
    })
    @PostMapping("/login")
    public LoginResponse login(
            @Valid @RequestBody LoginRequest request) {

        return userService.loginUser(
                request.email(),
                request.password()
        );
    }

    @Operation(
            summary = "Get current user",
            description = "Returns the authenticated user's profile information."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Current user retrieved successfully"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication required"
            )
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/me")
    public LoginResponse getCurrentUser(
            Authentication authentication) {

        return userService.getCurrentUser(
                authentication.getName()
        );
    }

    @Operation(
            summary = "Verify email address",
            description = "Verifies a local account using the token sent by email."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Email verified successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid or expired verification token"
            )
    })
    @GetMapping("/verify-email")
    @ResponseStatus(HttpStatus.OK)
    public Map<String, String> verifyEmail(
            @Parameter(
                    description = "Email verification token",
                    example = "verification-token"
            )
            @RequestParam String token) {

        emailVerificationService.verifyEmail(token);

        return Map.of(
                "message",
                "Email verified successfully"
        );
    }

    @Operation(
            summary = "Resend verification email",
            description = "Sends a new verification email when the account exists and is not already verified."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Verification request processed"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Unable to process the verification request"
            )
    })
    @PostMapping("/resend-verification")
    @ResponseStatus(HttpStatus.OK)
    public Map<String, String> resendVerificationEmail(
            @Parameter(
                    description = "Email address of the account",
                    example = "user@example.com"
            )
            @RequestParam String email) {

        userService.resendVerificationEmail(email);

        return Map.of(
                "message",
                "If the account exists and is not verified, "
                        + "a new verification email has been sent."
        );
    }

    @Operation(
            summary = "Request password reset",
            description = "Starts the password reset process. "
                    + "The response intentionally does not reveal whether the email belongs to an account."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Password reset request processed"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid email address"
            )
    })
    @PostMapping("/forgot-password")
    @ResponseStatus(HttpStatus.OK)
    public Map<String, String> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {

        passwordResetService.requestPasswordReset(
                request.email()
        );

        return Map.of(
                "message",
                "If an account exists for this email, "
                        + "a password reset link has been sent."
        );
    }

    @Operation(
            summary = "Reset password",
            description = "Resets the user's password using a valid, non-expired reset token."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Password reset successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid, expired or missing reset token, or invalid password"
            )
    })
    @PostMapping("/reset-password")
    @ResponseStatus(HttpStatus.OK)
    public Map<String, String> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {

        passwordResetService.resetPassword(
                request.token(),
                request.newPassword()
        );

        return Map.of(
                "message",
                "Password reset successfully"
        );
    }
}