package com.examforge.api.attempt.grading;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

class GradingCalculatorTest {

    private final GradingCalculator calculator = new GradingCalculator();

    @Test
    void allCorrectAnswersGiveMaximumScore() {
        GradingResult result = calculator.grade(List.of(
                new GradedAnswer(1L, "Célula", true),
                new GradedAnswer(2L, "Célula", true),
                new GradedAnswer(3L, "ADN", true)));

        assertThat(result.correctCount()).isEqualTo(3);
        assertThat(result.totalQuestions()).isEqualTo(3);
        assertThat(result.score()).isEqualByComparingTo("20");
    }

    @Test
    void partialAnswersAreScaledToTwenty() {
        GradingResult result = calculator.grade(List.of(
                new GradedAnswer(1L, "A", true),
                new GradedAnswer(2L, "A", true),
                new GradedAnswer(3L, "A", true),
                new GradedAnswer(4L, "A", false)));

        assertThat(result.score()).isEqualByComparingTo("15");
    }

    @Test
    void groupsByTopicWithWeakestTopicFirst() {
        GradingResult result = calculator.grade(List.of(
                new GradedAnswer(1L, "Fotosíntesis", true),
                new GradedAnswer(2L, "Fotosíntesis", true),
                new GradedAnswer(3L, "Mitosis", true),
                new GradedAnswer(4L, "Mitosis", false)));

        assertThat(result.performanceByTopic()).hasSize(2);
        TopicPerformance weakest = result.performanceByTopic().get(0);
        assertThat(weakest.topic()).isEqualTo("Mitosis");
        assertThat(weakest.correct()).isEqualTo(1);
        assertThat(weakest.total()).isEqualTo(2);
        assertThat(weakest.percentage()).isEqualByComparingTo("50");
    }

    @Test
    void blankTopicIsGroupedAsGeneral() {
        GradingResult result = calculator.grade(List.of(
                new GradedAnswer(1L, null, true),
                new GradedAnswer(2L, "  ", false)));

        assertThat(result.performanceByTopic())
                .extracting(TopicPerformance::topic)
                .containsExactly("General");
    }

    @Test
    void emptyAttemptScoresZero() {
        GradingResult result = calculator.grade(List.of());

        assertThat(result.score()).isEqualByComparingTo("0");
        assertThat(result.performanceByTopic()).isEmpty();
    }
}
