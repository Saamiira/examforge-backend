package com.examforge.api.ai;

import static org.assertj.core.api.Assertions.assertThat;

import com.examforge.api.ai.client.EmbeddingClient;
import com.examforge.api.ai.client.LlmClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@EnabledIfEnvironmentVariable(named = "AI_API_KEY", matches = ".+")
class AiClientSmokeTest {

    @Autowired
    private EmbeddingClient embeddingClient;

    @Autowired
    private LlmClient llmClient;

    @Test
    void embedsTextIntoVector() {
        float[] vector = embeddingClient.embed("La fotosíntesis convierte la luz en energía química.");

        assertThat(vector).hasSize(3072);
    }

    @Test
    void generatesJsonResponse() {
        String json = llmClient.generateJson(
                "Responde solo con JSON válido.",
                "Devuelve exactamente este objeto: {\"ok\": true}");

        assertThat(json).startsWith("{").contains("ok");
    }
}
