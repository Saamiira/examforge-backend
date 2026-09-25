package com.examforge.api.ai.client;

import java.util.List;

public interface EmbeddingClient {

    List<float[]> embed(List<String> texts);

    default float[] embed(String text) {
        return embed(List.of(text)).get(0);
    }
}
