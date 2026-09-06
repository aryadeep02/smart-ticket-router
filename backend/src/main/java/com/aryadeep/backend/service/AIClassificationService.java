package com.aryadeep.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.aryadeep.backend.dto.AIClassificationRequest;
import com.aryadeep.backend.dto.AIClassificationResponse;

@Service
public class AIClassificationService {

    private final RestClient restClient;

    public AIClassificationService(
            @Value("${ai.service.url}") String aiServiceUrl) {

        this.restClient = RestClient.builder()
                .baseUrl(aiServiceUrl)
                .build();
    }

    public AIClassificationResponse classify(
            String title,
            String description) {

        AIClassificationRequest request =
                new AIClassificationRequest(title, description);

        return restClient.post()
                .uri("/api/v1/classify")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(AIClassificationResponse.class);
    }
}