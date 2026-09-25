package com.examforge.api.attempt.grading;

import java.math.BigDecimal;

public record TopicPerformance(String topic, int correct, int total, BigDecimal percentage) {
}
