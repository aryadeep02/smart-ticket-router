package com.aryadeep.backend.dto;

public record AIClassificationResponse(
        String category,
        String priority,
        Double confidence
) {}