package com.examforge.api.ai.dto;

import java.util.List;

public record EmbeddingRequest(String model, List<String> input) {
}
