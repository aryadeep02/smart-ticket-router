package com.aryadeep.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateTicketRequest(

        @NotBlank(message = "Title is required")
        @Size(max = 200, message = "Title must not exceed 200 characters")
        String title,

        @NotBlank(message = "Description is required")
        @Size(max = 5000, message = "Description must not exceed 5000 characters")
        String description
) {
}