package com.examforge.api.attempt.service;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.examforge.api.academic.repository.UserCourseRepository;
import com.examforge.api.assessment.entity.Assessment;
import com.examforge.api.assessment.entity.AssessmentStatus;
import com.examforge.api.assessment.entity.Option;
import com.examforge.api.assessment.entity.Question;
import com.examforge.api.assessment.entity.Visibility;
import com.examforge.api.assessment.repository.AssessmentRepository;
import com.examforge.api.attempt.dto.AnswerRequest;
import com.examforge.api.attempt.dto.AttemptResultResponse;
import com.examforge.api.attempt.dto.AttemptStartResponse;
import com.examforge.api.attempt.dto.AttemptSubmitRequest;
import com.examforge.api.attempt.dto.AttemptSummaryResponse;
import com.examforge.api.attempt.entity.Answer;
import com.examforge.api.attempt.entity.Attempt;
import com.examforge.api.attempt.grading.GradedAnswer;
import com.examforge.api.attempt.grading.GradingCalculator;
import com.examforge.api.attempt.grading.GradingResult;
import com.examforge.api.attempt.mapper.AttemptMapper;
import com.examforge.api.attempt.repository.AttemptRepository;
import com.examforge.api.common.dto.PageResponse;
import com.examforge.api.common.exception.AttemptAlreadySubmittedException;
import com.examforge.api.common.exception.BadRequestException;
import com.examforge.api.common.exception.ForbiddenException;
import com.examforge.api.common.exception.ResourceNotFoundException;
import com.examforge.api.notification.event.AttemptCompletedEvent;
import com.examforge.api.security.CurrentUserProvider;
import com.examforge.api.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AttemptService {

    private final AttemptRepository attemptRepository;
    private final AssessmentRepository assessmentRepository;
    private final CurrentUserProvider currentUserProvider;
    private final GradingCalculator gradingCalculator;
    private final AttemptMapper attemptMapper;
    private final ApplicationEventPublisher eventPublisher;
    private final UserCourseRepository userCourseRepository;

    @Transactional
    public AttemptStartResponse start(Long assessmentId) {
        User student = currentUserProvider.getCurrentUser();
        Assessment assessment = assessmentRepository.findWithQuestionsById(assessmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assessment", assessmentId));
        checkCanTake(assessment, student.getId());

        Attempt attempt = attemptRepository.save(new Attempt(student, assessment));
        return attemptMapper.toStartResponse(attempt, assessment.getQuestions());
    }

    @Transactional
    public AttemptResultResponse submit(Long attemptId, AttemptSubmitRequest request) {
        Attempt attempt = findOwnedAttempt(attemptId);
        if (attempt.isSubmitted()) {
            throw new AttemptAlreadySubmittedException("Attempt " + attemptId + " was already submitted");
        }

        Long assessmentId = attempt.getAssessment().getId();
        List<Question> questions = assessmentRepository.findWithQuestionsById(assessmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assessment", assessmentId))
                .getQuestions();
        Map<Long, AnswerRequest> answersByQuestion = indexAnswers(request.answers(), questions);

        for (Question question : questions) {
            AnswerRequest answer = answersByQuestion.get(question.getId());
            Option selected = answer == null ? null : findOption(question, answer.selectedOptionId());
            attempt.addAnswer(new Answer(question, selected));
        }

        GradingResult result = gradingCalculator.grade(toGradedAnswers(attempt));
        attempt.submit(result.score(), result.correctCount(), result.totalQuestions());
        attemptRepository.save(attempt);

        eventPublisher.publishEvent(new AttemptCompletedEvent(attempt.getId(), attempt.getStudent().getId()));
        return attemptMapper.toResultResponse(attempt, result.performanceByTopic());
    }

    @Transactional(readOnly = true)
    public AttemptResultResponse getResult(Long attemptId) {
        Attempt attempt = findOwnedAttempt(attemptId);
        if (!attempt.isSubmitted()) {
            throw new BadRequestException("Attempt " + attemptId + " has not been submitted yet");
        }
        GradingResult result = gradingCalculator.grade(toGradedAnswers(attempt));
        return attemptMapper.toResultResponse(attempt, result.performanceByTopic());
    }

    @Transactional(readOnly = true)
    public PageResponse<AttemptSummaryResponse> findMine(Pageable pageable) {
        Long studentId = currentUserProvider.getCurrentUserId();
        return PageResponse.from(attemptRepository.findByStudentIdOrderByCreatedAtDesc(studentId, pageable)
                .map(attemptMapper::toSummary));
    }

    private Attempt findOwnedAttempt(Long attemptId) {
        Attempt attempt = attemptRepository.findWithAssessmentById(attemptId)
                .orElseThrow(() -> new ResourceNotFoundException("Attempt", attemptId));
        if (!attempt.belongsTo(currentUserProvider.getCurrentUserId())) {
            throw new ForbiddenException("You can only access your own attempts");
        }
        return attempt;
    }

    private void checkCanTake(Assessment assessment, Long userId) {
        boolean isAuthor = assessment.isAuthoredBy(userId);
        boolean available = assessment.getStatus() == AssessmentStatus.PUBLISHED
                || (isAuthor && assessment.getStatus() == AssessmentStatus.READY);
        if (!available) {
            throw new BadRequestException("Assessment " + assessment.getId() + " is not available to be taken");
        }
        if (assessment.getVisibility() == Visibility.PRIVATE && !isAuthor) {
            throw new ForbiddenException("This assessment is private");
        }
        if (assessment.getVisibility() == Visibility.COURSE && !isAuthor
                && !userCourseRepository.existsByUserIdAndCourseId(userId, assessment.getCourse().getId())) {
            throw new ForbiddenException("Enroll in the course to take this assessment");
        }
        if (assessment.getQuestions().isEmpty()) {
            throw new BadRequestException("Assessment " + assessment.getId() + " has no questions");
        }
    }

    private Map<Long, AnswerRequest> indexAnswers(List<AnswerRequest> answers, List<Question> questions) {
        Set<Long> questionIds = questions.stream().map(Question::getId).collect(Collectors.toSet());
        Set<Long> seen = new HashSet<>();
        for (AnswerRequest answer : answers) {
            if (!questionIds.contains(answer.questionId())) {
                throw new BadRequestException("Question " + answer.questionId() + " does not belong to this assessment");
            }
            if (!seen.add(answer.questionId())) {
                throw new BadRequestException("Question " + answer.questionId() + " was answered more than once");
            }
        }
        return answers.stream().collect(Collectors.toMap(AnswerRequest::questionId, Function.identity()));
    }

    private Option findOption(Question question, Long optionId) {
        if (optionId == null) {
            return null;
        }
        return question.getOptions().stream()
                .filter(option -> option.getId().equals(optionId))
                .findFirst()
                .orElseThrow(() -> new BadRequestException(
                        "Option " + optionId + " does not belong to question " + question.getId()));
    }

    private List<GradedAnswer> toGradedAnswers(Attempt attempt) {
        return attempt.getAnswers().stream()
                .map(answer -> new GradedAnswer(
                        answer.getQuestion().getId(), answer.getQuestion().getTopic(), answer.isCorrect()))
                .toList();
    }
}