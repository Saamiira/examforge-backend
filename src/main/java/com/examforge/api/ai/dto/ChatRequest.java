package com.examforge.api.ai.dto;

import java.util.List;

public record ChatRequest(String model, List<ChatMessage> messages, double temperature) {
}
