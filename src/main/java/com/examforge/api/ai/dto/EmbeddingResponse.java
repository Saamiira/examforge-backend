package com.examforge.api.ai.dto;

import java.util.List;

public record EmbeddingResponse(List<Data> data) {

    public record Data(int index, float[] embedding) {
    }
}
