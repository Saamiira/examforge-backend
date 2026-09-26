package com.examforge.api.attempt.mapper;

import java.util.Comparator;
import java.util.List;

import com.examforge.api.assessment.entity.Option;
import com.examforge.api.assessment.entity.Question;
import com.examforge.api.attempt.dto.AttemptOptionResponse;
import com.examforge.api.attempt.dto.AttemptQuestionResponse;
import com.examforge.api.attempt.dto.AttemptResultResponse;
import com.examforge.api.attempt.dto.AttemptStartResponse;
import com.examforge.api.attempt.dto.AttemptSummaryResponse;
import com.examforge.api.attempt.dto.QuestionFeedbackResponse;
import com.examforge.api.attempt.entity.Answer;
import com.examforge.api.attempt.entity.Attempt;
import com.examforge.api.attempt.grading.TopicPerformance;
import org.springframework.stereotype.Component;

/**
 * Builds attempt responses. Questions shown while taking an attempt never expose
 * which option is correct; the correct option is only revealed in the result feedback.
 */
@Component
public class AttemptMapper {

    public AttemptStartResponse toStartResponse(Attempt attempt, List<Question> questions) {
        List<AttemptQuestionResponse> items = questions.stream()
                .sorted(Comparator.comparingInt(Question::getPosition))
                .map(this::toQuestion)
                .toList();
        return new AttemptStartResponse(
                attempt.getId(),
                attempt.getAssessment().getId(),
                attempt.getAssessment().getTitle(),
                attempt.getStatus(),
                attempt.getStartedAt(),
                items);
    }

    public AttemptResultResponse toResultResponse(Attempt attempt, List<TopicPerformance> performanceByTopic) {
        List<QuestionFeedbackResponse> feedback = attempt.getAnswers().stream()
                .sorted(Comparator.comparingInt(answer -> answer.getQuestion().getPosition()))
                .map(this::toFeedback)
                .toList();
        return new AttemptResultResponse(
                attempt.getId(),
                attempt.getAssessment().getId(),
                attempt.getStatus(),
                attempt.getStartedAt(),
                attempt.getSubmittedAt(),
                attempt.getCorrectCount(),
                attempt.getTotalQuestions(),
                attempt.getScore(),
                performanceByTopic,
                feedback);
    }

    public AttemptSummaryResponse toSummary(Attempt attempt) {
        return new AttemptSummaryResponse(
                attempt.getId(),
                attempt.getAssessment().getId(),
                attempt.getAssessment().getTitle(),
                attempt.getStatus(),
                attempt.getScore(),
                attempt.getSubmittedAt());
    }

    private AttemptQuestionResponse toQuestion(Question question) {
        List<AttemptOptionResponse> options = question.getOptions().stream()
                .sorted(Comparator.comparingInt(Option::getPosition))
                .map(option -> new AttemptOptionResponse(option.getId(), option.getText()))
                .toList();
        return new AttemptQuestionResponse(
                question.getId(), question.getText(), question.getType(), question.getPosition(), options);
    }

    private QuestionFeedbackResponse toFeedback(Answer answer) {
        Question question = answer.getQuestion();
        Long correctOptionId = question.getOptions().stream()
                .filter(Option::isCorrect)
                .map(Option::getId)
                .findFirst()
                .orElse(null);
        Long selectedOptionId = answer.getSelectedOption() == null ? null : answer.getSelectedOption().getId();
        return new QuestionFeedbackResponse(
                question.getId(),
                question.getText(),
                question.getTopic(),
                selectedOptionId,
                correctOptionId,
                answer.isCorrect(),
                question.getExplanation());
    }
}
