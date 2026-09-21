package com.aryadeep.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
        name = "SupportTeamResponse",
        description = "Support team information returned by the API"
)
public record SupportTeamResponse(

        @Schema(
                description = "Unique identifier of the support team",
                example = "2"
        )
        Long id,

        @Schema(
                description = "Unique support team name",
                example = "PAYMENT_SUPPORT"
        )
        String name,

        @Schema(
                description = "Description of the support team's responsibility",
                example = "Handles payment and refund related tickets"
        )
        String description

) {
}