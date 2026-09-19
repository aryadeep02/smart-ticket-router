package com.aryadeep.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final String mailFrom;
    private final String frontendUrl;

    public EmailService(
            JavaMailSender mailSender,
            @Value("${spring.mail.username}") String mailFrom,
            @Value("${frontend.url}") String frontendUrl) {

        if (mailFrom == null || mailFrom.isBlank()) {
            throw new IllegalStateException(
                    "spring.mail.username must be configured"
            );
        }

        this.mailSender = mailSender;
        this.mailFrom = mailFrom;
        this.frontendUrl = frontendUrl;
    }

    public void sendVerificationEmail(
            String recipientEmail,
            String recipientName,
            String verificationToken) {

        String verificationUrl =
                frontendUrl
                        + "/verify-email?token="
                        + verificationToken;

        SimpleMailMessage message =
                new SimpleMailMessage();

        message.setFrom(mailFrom);
        message.setTo(recipientEmail);
        message.setSubject(
                "Verify your Smart Ticket Router account");

        message.setText(
                "Hi " + recipientName + ",\n\n"
                        + "Welcome to Smart Ticket Router!\n\n"
                        + "Please verify your email address by clicking "
                        + "the link below:\n\n"
                        + verificationUrl
                        + "\n\n"
                        + "This verification link expires in 24 hours.\n\n"
                        + "If you did not create this account, you can "
                        + "safely ignore this email.\n\n"
                        + "Regards,\n"
                        + "Smart Ticket Router");

        mailSender.send(message);
    }

    public void sendPasswordResetEmail(
            String recipientEmail,
            String recipientName,
            String resetToken) {

        String resetUrl =
                frontendUrl
                        + "/reset-password?token="
                        + resetToken;

        SimpleMailMessage message =
                new SimpleMailMessage();

        message.setFrom(mailFrom);
        message.setTo(recipientEmail);
        message.setSubject(
                "Reset your Smart Ticket Router password");

        message.setText(
                "Hi " + recipientName + ",\n\n"
                        + "We received a request to reset your "
                        + "Smart Ticket Router password.\n\n"
                        + "You can reset your password by clicking "
                        + "the link below:\n\n"
                        + resetUrl
                        + "\n\n"
                        + "This password reset link expires in 1 hour.\n\n"
                        + "If you did not request a password reset, "
                        + "you can safely ignore this email.\n\n"
                        + "Regards,\n"
                        + "Smart Ticket Router");

        mailSender.send(message);
    }
}