package com.examforge.api.attempt.grading;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

@Component
public class GradingCalculator {

    private static final String DEFAULT_TOPIC = "General";
    private static final BigDecimal MAX_SCORE = BigDecimal.valueOf(20);
    private static final int SCALE = 2;

    public GradingResult grade(List<GradedAnswer> answers) {
        int total = answers.size();
        int correct = countCorrect(answers);
        return new GradingResult(correct, total, score(correct, total), performanceByTopic(answers));
    }

    private BigDecimal score(int correct, int total) {
        if (total == 0) {
            return BigDecimal.ZERO.setScale(SCALE);
        }
        return MAX_SCORE.multiply(BigDecimal.valueOf(correct))
                .divide(BigDecimal.valueOf(total), SCALE, RoundingMode.HALF_UP);
    }

    private List<TopicPerformance> performanceByTopic(List<GradedAnswer> answers) {
        Map<String, List<GradedAnswer>> byTopic = answers.stream()
                .collect(Collectors.groupingBy(this::topicOf, LinkedHashMap::new, Collectors.toList()));
        return byTopic.entrySet().stream()
                .map(entry -> toPerformance(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparing(TopicPerformance::percentage))
                .toList();
    }

    private TopicPerformance toPerformance(String topic, List<GradedAnswer> answers) {
        int correct = countCorrect(answers);
        BigDecimal percentage = BigDecimal.valueOf(correct * 100L)
                .divide(BigDecimal.valueOf(answers.size()), SCALE, RoundingMode.HALF_UP);
        return new TopicPerformance(topic, correct, answers.size(), percentage);
    }

    private int countCorrect(List<GradedAnswer> answers) {
        return (int) answers.stream().filter(GradedAnswer::correct).count();
    }

    private String topicOf(GradedAnswer answer) {
        return answer.topic() == null || answer.topic().isBlank() ? DEFAULT_TOPIC : answer.topic().strip();
    }
}
