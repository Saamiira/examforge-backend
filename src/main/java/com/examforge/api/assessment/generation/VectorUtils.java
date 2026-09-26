package com.examforge.api.assessment.generation;

/**
 * Helpers to send embeddings to pgvector and to score retrieved fragments.
 */
public final class VectorUtils {

    private VectorUtils() {
    }

    /** Formats an embedding as a pgvector literal, e.g. {@code [0.12,-0.3,0.5]}. */
    public static String toPgVector(float[] vector) {
        StringBuilder builder = new StringBuilder(vector.length * 12).append('[');
        for (int i = 0; i < vector.length; i++) {
            if (i > 0) {
                builder.append(',');
            }
            builder.append(vector[i]);
        }
        return builder.append(']').toString();
    }

    /** Cosine similarity in [-1, 1]; pgvector's {@code <=>} operator returns {@code 1 - similarity}. */
    public static double cosineSimilarity(float[] a, float[] b) {
        if (a == null || b == null || a.length != b.length) {
            return 0.0;
        }
        double dot = 0.0;
        double normA = 0.0;
        double normB = 0.0;
        for (int i = 0; i < a.length; i++) {
            dot += a[i] * b[i];
            normA += a[i] * a[i];
            normB += b[i] * b[i];
        }
        if (normA == 0.0 || normB == 0.0) {
            return 0.0;
        }
        return dot / (Math.sqrt(normA) * Math.sqrt(normB));
    }
}
