package com.examforge.api.attempt.grading;

public record GradedAnswer(Long questionId, String topic, boolean correct) {
}
