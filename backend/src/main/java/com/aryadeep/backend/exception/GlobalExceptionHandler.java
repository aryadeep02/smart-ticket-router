package com.aryadeep.backend.exception;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

        @ExceptionHandler(EmailAlreadyExistsException.class)
        @ResponseStatus(HttpStatus.CONFLICT)
        public Map<String, String> handleEmailAlreadyExists(
                        EmailAlreadyExistsException exception) {

                return Map.of(
                                "message",
                                exception.getMessage());
        }

        @ExceptionHandler(InvalidCredentialsException.class)
        @ResponseStatus(HttpStatus.UNAUTHORIZED)
        public Map<String, String> handleInvalidCredentials(
                        InvalidCredentialsException exception) {

                return Map.of(
                                "message",
                                exception.getMessage());
        }

        @ExceptionHandler(IllegalStateException.class)
        @ResponseStatus(HttpStatus.BAD_REQUEST)
        public Map<String, String> handleIllegalState(
                        IllegalStateException exception) {

                return Map.of(
                                "message",
                                exception.getMessage());
        }

        @ExceptionHandler(AccessDeniedException.class)
        @ResponseStatus(HttpStatus.FORBIDDEN)
        public Map<String, String> handleAccessDenied(
                        AccessDeniedException exception) {

                return Map.of(
                                "message",
                                exception.getMessage());
        }
}