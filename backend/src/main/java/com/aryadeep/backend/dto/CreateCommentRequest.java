package com.aryadeep.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(
        name = "CreateCommentRequest",
        description = "Request used to add a comment to a support ticket"
)
public record CreateCommentRequest(

        @Schema(
                description = "Text content of the ticket comment",
                example = "I have checked the transaction and it is still showing as failed.",
                requiredMode = Schema.RequiredMode.REQUIRED,
                maxLength = 5000
        )
        @NotBlank(message = "Comment content is required")
        @Size(
                max = 5000,
                message = "Comment cannot exceed 5000 characters"
        )
        String content

) {
}