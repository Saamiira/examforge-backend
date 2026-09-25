package com.examforge.api.ai.client;

import java.util.List;

import com.examforge.api.ai.AiProperties;
import com.examforge.api.ai.dto.ChatMessage;
import com.examforge.api.ai.dto.ChatRequest;
import com.examforge.api.ai.dto.ChatResponse;
import com.examforge.api.common.exception.AiGenerationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Slf4j
@Component
@RequiredArgsConstructor
public class OpenAiCompatibleLlmClient implements LlmClient {

    private static final double TEMPERATURE = 0.3;
    private static final int MAX_ATTEMPTS = 3;
    private static final long BACKOFF_MILLIS = 2000;

    private final RestClient aiRestClient;
    private final AiProperties properties;

    @Override
    public String generateJson(String systemPrompt, String userPrompt) {
        ChatRequest request = new ChatRequest(
                properties.chatModel(),
                List.of(ChatMessage.system(systemPrompt), ChatMessage.user(userPrompt)),
                TEMPERATURE);
        for (int attempt = 1; ; attempt++) {
            try {
                return stripCodeFences(extractContent(callProvider(request)));
            } catch (HttpServerErrorException | HttpClientErrorException.TooManyRequests ex) {
                if (attempt >= MAX_ATTEMPTS) {
                    log.warn("Chat completion unavailable after {} attempts: {}", attempt, ex.getStatusCode());
                    throw new AiGenerationException("Language model is temporarily unavailable");
                }
                log.info("Retrying chat completion ({}/{}) after {}", attempt, MAX_ATTEMPTS, ex.getStatusCode());
                pause(BACKOFF_MILLIS * attempt);
            } catch (RestClientException ex) {
                log.warn("Chat completion request failed: {}", ex.getMessage());
                throw new AiGenerationException("Language model request failed");
            }
        }
    }

    private ChatResponse callProvider(ChatRequest request) {
        return aiRestClient.post()
                .uri("/chat/completions")
                .body(request)
                .retrieve()
                .body(ChatResponse.class);
    }

    private String extractContent(ChatResponse response) {
        boolean empty = response == null || response.choices() == null || response.choices().isEmpty()
                || response.choices().get(0).message() == null
                || response.choices().get(0).message().content() == null;
        if (empty) {
            throw new AiGenerationException("Language model returned an empty response");
        }
        return response.choices().get(0).message().content();
    }

    private String stripCodeFences(String content) {
        String trimmed = content.strip();
        if (!trimmed.startsWith("```")) {
            return trimmed;
        }
        return trimmed.replaceFirst("^```(?:json)?\\s*", "").replaceFirst("\\s*```$", "").strip();
    }

    private void pause(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new AiGenerationException("Language model request was interrupted");
        }
    }
}
