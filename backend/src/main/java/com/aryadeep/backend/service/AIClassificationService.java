package com.aryadeep.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.aryadeep.backend.dto.AIClassificationRequest;
import com.aryadeep.backend.dto.AIClassificationResponse;


import java.net.http.HttpClient;
import java.time.Duration;
@Service
public class AIClassificationService {

        private final RestClient restClient;

        public AIClassificationService(
                @Value("${ai.service.url}") String aiServiceUrl) {
        
            HttpClient httpClient = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(2))
                    .build();
        
            JdkClientHttpRequestFactory requestFactory =
                    new JdkClientHttpRequestFactory(httpClient);
        
            requestFactory.setReadTimeout(Duration.ofSeconds(5));
        
            this.restClient = RestClient.builder()
                    .baseUrl(aiServiceUrl)
                    .requestFactory(requestFactory)
                    .build();
        }

        public AIClassificationResponse classify(
                        String title,
                        String description) {

                AIClassificationRequest request = new AIClassificationRequest(title, description);

                return restClient.post()
                                .uri("/api/v1/classify")
                                .contentType(MediaType.APPLICATION_JSON)
                                .body(request)
                                .retrieve()
                                .body(AIClassificationResponse.class);
        }
}