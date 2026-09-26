package com.examforge.api.assessment.generation;

import java.util.List;
import java.util.stream.Collectors;

import com.examforge.api.assessment.dto.AssessmentGenerateRequest;
import org.springframework.stereotype.Component;

@Component
public class AssessmentPromptBuilder {

    private static final String SYSTEM_PROMPT = """
            Eres un docente universitario experto en diseñar evaluaciones.
            Genera preguntas usando EXCLUSIVAMENTE la información de los fragmentos proporcionados.
            Responde SOLO con un objeto JSON válido, sin texto adicional ni bloques de código, con esta forma:
            {"questions":[{"text":"...","type":"MULTIPLE_CHOICE","difficulty":"MEDIUM","topic":"...",
            "explanation":"...","options":[{"text":"...","correct":true}],"sourceChunkIds":[1]}]}
            Reglas:
            - MULTIPLE_CHOICE: exactamente 4 opciones y solo una correcta.
            - TRUE_FALSE: exactamente 2 opciones, "Verdadero" y "Falso", y solo una correcta.
            - "sourceChunkIds" contiene los ids de los fragmentos que sustentan la pregunta.
            - "topic" es el tema concreto evaluado, en pocas palabras.
            - "explanation" justifica la respuesta correcta citando el contenido.
            - Redacta en español, de forma clara y sin ambigüedades.
            """;

    public String systemPrompt() {
        return SYSTEM_PROMPT;
    }

    public String buildUserPrompt(AssessmentGenerateRequest request, List<ContextChunk> chunks) {
        return """
                Genera %d preguntas.
                Dificultad: %s
                Tipos permitidos: %s
                Enfoque: %s

                Fragmentos:
                %s
                """.formatted(
                request.questionCount(),
                request.difficulty(),
                request.questionTypes(),
                focusOrDefault(request.focusTopic()),
                formatChunks(chunks));
    }

    private String focusOrDefault(String focusTopic) {
        return focusTopic == null || focusTopic.isBlank() ? "todo el contenido" : focusTopic;
    }

    private String formatChunks(List<ContextChunk> chunks) {
        return chunks.stream()
                .map(chunk -> "[id=%d | página %s]%n%s".formatted(
                        chunk.chunkId(),
                        chunk.pageNumber() == null ? "?" : chunk.pageNumber(),
                        chunk.content()))
                .collect(Collectors.joining("\n\n"));
    }
}
