package com.aryadeep.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
        name = "AIClassificationResponse",
        description = "Classification result returned by the AI service"
)
public record AIClassificationResponse(

        @Schema(
                description = "Predicted ticket category",
                example = "PAYMENT"
        )
        String category,

        @Schema(
                description = "Predicted ticket priority",
                example = "HIGH"
        )
        String priority,

        @Schema(
                description = "Model confidence score for the classification",
                example = "0.92",
                minimum = "0.0",
                maximum = "1.0"
        )
        Double confidence

) {
}