package com.examforge.api.ai.client;

public interface LlmClient {

    String generateJson(String systemPrompt, String userPrompt);
}
