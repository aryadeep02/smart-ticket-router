package com.aryadeep.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
        name = "AIClassificationRequest",
        description = "Input sent to the AI classification service for ticket categorization and priority prediction"
)
public record AIClassificationRequest(

        @Schema(
                description = "Ticket title used by the classification model",
                example = "Payment failed during checkout"
        )
        String title,

        @Schema(
                description = "Ticket description used by the classification model",
                example = "My card was charged but the checkout failed."
        )
        String description

) {
}