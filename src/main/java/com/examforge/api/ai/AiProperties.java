package com.examforge.api.ai;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.ai")
public record AiProperties(
        String apiKey,
        String baseUrl,
        String chatModel,
        String embeddingModel
) {
}
