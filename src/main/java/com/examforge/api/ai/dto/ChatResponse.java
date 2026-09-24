package com.examforge.api.ai.dto;

import java.util.List;

public record ChatResponse(List<Choice> choices) {

    public record Choice(ChatMessage message) {
    }
}
