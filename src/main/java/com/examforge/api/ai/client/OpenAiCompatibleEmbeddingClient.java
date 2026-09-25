package com.examforge.api.ai.client;

import java.util.Comparator;
import java.util.List;

import com.examforge.api.ai.AiProperties;
import com.examforge.api.ai.dto.EmbeddingRequest;
import com.examforge.api.ai.dto.EmbeddingResponse;
import com.examforge.api.common.exception.AiGenerationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Slf4j
@Component
@RequiredArgsConstructor
public class OpenAiCompatibleEmbeddingClient implements EmbeddingClient {

    private final RestClient aiRestClient;
    private final AiProperties properties;

    @Override
    public List<float[]> embed(List<String> texts) {
        EmbeddingRequest request = new EmbeddingRequest(properties.embeddingModel(), texts);
        try {
            EmbeddingResponse response = aiRestClient.post()
                    .uri("/embeddings")
                    .body(request)
                    .retrieve()
                    .body(EmbeddingResponse.class);
            return toVectors(response, texts.size());
        } catch (RestClientException ex) {
            log.warn("Embedding request failed: {}", ex.getMessage());
            throw new AiGenerationException("Embedding provider request failed");
        }
    }

    private List<float[]> toVectors(EmbeddingResponse response, int expectedSize) {
        if (response == null || response.data() == null || response.data().size() != expectedSize) {
            throw new AiGenerationException("Embedding provider returned an incomplete response");
        }
        return response.data().stream()
                .sorted(Comparator.comparingInt(EmbeddingResponse.Data::index))
                .map(EmbeddingResponse.Data::embedding)
                .toList();
    }
}
