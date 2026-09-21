package com.aryadeep.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(
        name = "CreateTicketRequest",
        description = "Request payload used to create a new support ticket"
)
public record CreateTicketRequest(

        @Schema(
                description = "Short title describing the customer's issue",
                example = "Payment failed during checkout",
                requiredMode = Schema.RequiredMode.REQUIRED,
                maxLength = 200
        )
        @NotBlank(message = "Title is required")
        @Size(
                max = 200,
                message = "Title must not exceed 200 characters"
        )
        String title,

        @Schema(
                description = "Detailed explanation of the customer's issue",
                example = "My card was charged but the order was not completed.",
                requiredMode = Schema.RequiredMode.REQUIRED,
                maxLength = 5000
        )
        @NotBlank(message = "Description is required")
        @Size(
                max = 5000,
                message = "Description must not exceed 5000 characters"
        )
        String description
) {
}