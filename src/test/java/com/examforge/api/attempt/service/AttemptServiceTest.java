package com.examforge.api.attempt.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import com.examforge.api.academic.entity.Course;
import com.examforge.api.academic.repository.UserCourseRepository;
import com.examforge.api.assessment.entity.Assessment;
import com.examforge.api.assessment.entity.AssessmentStatus;
import com.examforge.api.assessment.entity.Option;
import com.examforge.api.assessment.entity.Question;
import com.examforge.api.assessment.entity.Visibility;
import com.examforge.api.assessment.repository.AssessmentRepository;
import com.examforge.api.attempt.dto.AnswerRequest;
import com.examforge.api.attempt.dto.AttemptResultResponse;
import com.examforge.api.attempt.dto.AttemptSubmitRequest;
import com.examforge.api.attempt.dto.QuestionFeedbackResponse;
import com.examforge.api.attempt.entity.Attempt;
import com.examforge.api.attempt.entity.AttemptStatus;
import com.examforge.api.attempt.grading.GradingCalculator;
import com.examforge.api.attempt.mapper.AttemptMapper;
import com.examforge.api.attempt.repository.AttemptRepository;
import com.examforge.api.common.exception.AttemptAlreadySubmittedException;
import com.examforge.api.common.exception.BadRequestException;
import com.examforge.api.common.exception.ForbiddenException;
import com.examforge.api.notification.event.AttemptCompletedEvent;
import com.examforge.api.security.CurrentUserProvider;
import com.examforge.api.user.entity.Role;
import com.examforge.api.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class AttemptServiceTest {

    @Mock
    private AttemptRepository attemptRepository;
    @Mock
    private AssessmentRepository assessmentRepository;
    @Mock
    private CurrentUserProvider currentUserProvider;
    @Mock
    private ApplicationEventPublisher eventPublisher;
    @Mock
    private UserCourseRepository userCourseRepository;

    private AttemptService service;
    private User student;
    private Assessment assessment;

    @BeforeEach
    void setUp() {
        service = new AttemptService(attemptRepository, assessmentRepository, currentUserProvider,
                new GradingCalculator(), new AttemptMapper(), eventPublisher, userCourseRepository);

        User author = withId(new User("Teacher", "teacher@examforge.dev", "x", Role.TEACHER), 1L);
        student = withId(new User("Student", "student@examforge.dev", "x", Role.STUDENT), 2L);

        assessment = withId(new Assessment(), 10L);
        assessment.setAuthor(author);
        assessment.setTitle("Normalization");
        assessment.setStatus(AssessmentStatus.PUBLISHED);
        assessment.setVisibility(Visibility.PUBLIC);
        assessment.addQuestion(question(100L, "1NF", 1000L, 1001L));
        assessment.addQuestion(question(200L, "2NF", 2000L, 2001L));
    }

    @Test
    void submitGradesAnswersOnVigesimalScaleAndPublishesEvent() {
        Attempt attempt = inProgressAttempt();
        when(currentUserProvider.getCurrentUserId()).thenReturn(student.getId());
        when(attemptRepository.findWithAssessmentById(50L)).thenReturn(Optional.of(attempt));
        when(assessmentRepository.findWithQuestionsById(10L)).thenReturn(Optional.of(assessment));

        AttemptResultResponse result = service.submit(50L, new AttemptSubmitRequest(List.of(
                new AnswerRequest(100L, 1000L),
                new AnswerRequest(200L, 2001L))));

        assertThat(result.status()).isEqualTo(AttemptStatus.SUBMITTED);
        assertThat(result.correctCount()).isEqualTo(1);
        assertThat(result.totalQuestions()).isEqualTo(2);
        assertThat(result.score()).isEqualByComparingTo(new BigDecimal("10.00"));
        assertThat(result.feedback()).extracting(QuestionFeedbackResponse::correctOptionId).containsExactly(1000L, 2000L);
        verify(eventPublisher).publishEvent(new AttemptCompletedEvent(50L, student.getId()));
    }

    @Test
    void unansweredQuestionsCountAsIncorrect() {
        Attempt attempt = inProgressAttempt();
        when(currentUserProvider.getCurrentUserId()).thenReturn(student.getId());
        when(attemptRepository.findWithAssessmentById(50L)).thenReturn(Optional.of(attempt));
        when(assessmentRepository.findWithQuestionsById(10L)).thenReturn(Optional.of(assessment));

        AttemptResultResponse result = service.submit(50L,
                new AttemptSubmitRequest(List.of(new AnswerRequest(100L, 1000L))));

        assertThat(result.totalQuestions()).isEqualTo(2);
        assertThat(result.score()).isEqualByComparingTo(new BigDecimal("10.00"));
    }

    @Test
    void submitRejectsOptionFromAnotherQuestion() {
        Attempt attempt = inProgressAttempt();
        when(currentUserProvider.getCurrentUserId()).thenReturn(student.getId());
        when(attemptRepository.findWithAssessmentById(50L)).thenReturn(Optional.of(attempt));
        when(assessmentRepository.findWithQuestionsById(10L)).thenReturn(Optional.of(assessment));

        assertThatThrownBy(() -> service.submit(50L,
                new AttemptSubmitRequest(List.of(new AnswerRequest(100L, 2000L)))))
                .isInstanceOf(BadRequestException.class);
        verify(eventPublisher, never()).publishEvent(any(Object.class));
    }

    @Test
    void submitTwiceIsRejected() {
        Attempt attempt = inProgressAttempt();
        attempt.submit(BigDecimal.TEN, 1, 2);
        when(currentUserProvider.getCurrentUserId()).thenReturn(student.getId());
        when(attemptRepository.findWithAssessmentById(50L)).thenReturn(Optional.of(attempt));

        assertThatThrownBy(() -> service.submit(50L, new AttemptSubmitRequest(List.of())))
                .isInstanceOf(AttemptAlreadySubmittedException.class);
    }

    @Test
    void anotherStudentCannotSubmit() {
        Attempt attempt = inProgressAttempt();
        when(currentUserProvider.getCurrentUserId()).thenReturn(99L);
        when(attemptRepository.findWithAssessmentById(50L)).thenReturn(Optional.of(attempt));

        assertThatThrownBy(() -> service.submit(50L, new AttemptSubmitRequest(List.of())))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void startHidesCorrectAnswersAndRejectsPrivateAssessments() {
        when(currentUserProvider.getCurrentUser()).thenReturn(student);
        when(assessmentRepository.findWithQuestionsById(10L)).thenReturn(Optional.of(assessment));
        when(attemptRepository.save(any(Attempt.class))).thenAnswer(inv -> withId(inv.getArgument(0), 50L));

        assertThat(service.start(10L).questions()).hasSize(2);

        assessment.setVisibility(Visibility.PRIVATE);
        assertThatThrownBy(() -> service.start(10L)).isInstanceOf(ForbiddenException.class);
    }

    @Test
    void courseAssessmentsRequireEnrollment() {
        assessment.setCourse(withId(new Course(), 5L));
        assessment.setVisibility(Visibility.COURSE);
        when(currentUserProvider.getCurrentUser()).thenReturn(student);
        when(assessmentRepository.findWithQuestionsById(10L)).thenReturn(Optional.of(assessment));
        when(userCourseRepository.existsByUserIdAndCourseId(2L, 5L)).thenReturn(false, true);
        when(attemptRepository.save(any(Attempt.class))).thenAnswer(inv -> withId(inv.getArgument(0), 50L));

        assertThatThrownBy(() -> service.start(10L)).isInstanceOf(ForbiddenException.class);
        assertThat(service.start(10L).questions()).hasSize(2);
    }

    private Attempt inProgressAttempt() {
        return withId(new Attempt(student, assessment), 50L);
    }

    private Question question(Long id, String topic, Long correctOptionId, Long wrongOptionId) {
        Question question = withId(new Question(), id);
        question.setText("Question about " + topic);
        question.setTopic(topic);
        question.addOption(option(correctOptionId, true));
        question.addOption(option(wrongOptionId, false));
        return question;
    }

    private Option option(Long id, boolean correct) {
        Option option = withId(new Option(), id);
        option.setText("Option " + id);
        option.setCorrect(correct);
        return option;
    }

    private static <T> T withId(T entity, Long id) {
        ReflectionTestUtils.setField(entity, "id", id);
        return entity;
    }
}