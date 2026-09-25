package com.examforge.api.assessment.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.examforge.api.academic.entity.Course;
import com.examforge.api.academic.repository.CourseRepository;
import com.examforge.api.assessment.dto.AssessmentGenerateRequest;
import com.examforge.api.assessment.dto.AssessmentSummaryResponse;
import com.examforge.api.assessment.dto.OptionRequest;
import com.examforge.api.assessment.dto.QuestionRequest;
import com.examforge.api.assessment.entity.Assessment;
import com.examforge.api.assessment.entity.AssessmentStatus;
import com.examforge.api.assessment.entity.Difficulty;
import com.examforge.api.assessment.entity.Question;
import com.examforge.api.assessment.entity.QuestionType;
import com.examforge.api.assessment.generation.AssessmentGenerationRequestedEvent;
import com.examforge.api.assessment.mapper.AssessmentMapper;
import com.examforge.api.assessment.mapper.QuestionMapper;
import com.examforge.api.assessment.mapper.QuestionMapperImpl;
import com.examforge.api.assessment.repository.AssessmentRepository;
import com.examforge.api.assessment.repository.QuestionSourceRepository;
import com.examforge.api.attempt.repository.AttemptRepository;
import com.examforge.api.common.exception.BadRequestException;
import com.examforge.api.common.exception.DuplicateResourceException;
import com.examforge.api.common.exception.ForbiddenException;
import com.examforge.api.document.entity.Document;
import com.examforge.api.document.entity.DocumentStatus;
import com.examforge.api.document.repository.DocumentRepository;
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
class AssessmentServiceTest {

    @Mock
    private AssessmentRepository assessmentRepository;
    @Mock
    private QuestionSourceRepository questionSourceRepository;
    @Mock
    private CourseRepository courseRepository;
    @Mock
    private DocumentRepository documentRepository;
    @Mock
    private AttemptRepository attemptRepository;
    @Mock
    private CurrentUserProvider currentUserProvider;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    private AssessmentService service;
    private User author;
    private User otherStudent;
    private Course course;
    private Assessment assessment;

    @BeforeEach
    void setUp() {
        QuestionMapper questionMapper = new QuestionMapperImpl();
        service = new AssessmentService(assessmentRepository, questionSourceRepository, courseRepository,
                documentRepository, attemptRepository, currentUserProvider,
                new AssessmentMapper(questionMapper), questionMapper, eventPublisher);

        author = withId(new User("Teacher", "teacher@examforge.dev", "x", Role.TEACHER), 1L);
        otherStudent = withId(new User("Student", "student@examforge.dev", "x", Role.STUDENT), 2L);
        course = withId(new Course(), 3L);

        assessment = withId(new Assessment(), 10L);
        assessment.setAuthor(author);
        assessment.setCourse(course);
        assessment.setTitle("Normalization");
        assessment.setDifficulty(Difficulty.EASY);
    }

    @Test
    void generationRejectsDocumentsThatAreNotReady() {
        when(currentUserProvider.getCurrentUser()).thenReturn(author);
        when(courseRepository.findById(3L)).thenReturn(Optional.of(course));
        when(documentRepository.findAllById(List.of(7L)))
                .thenReturn(List.of(document(7L, course, DocumentStatus.PROCESSING)));

        assertThatThrownBy(() -> service.requestGeneration(generateRequest()))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("not ready");
        verify(assessmentRepository, never()).save(any());
    }

    @Test
    void generationRejectsDocumentsFromAnotherCourse() {
        Course otherCourse = withId(new Course(), 4L);
        when(currentUserProvider.getCurrentUser()).thenReturn(author);
        when(courseRepository.findById(3L)).thenReturn(Optional.of(course));
        when(documentRepository.findAllById(List.of(7L)))
                .thenReturn(List.of(document(7L, otherCourse, DocumentStatus.READY)));

        assertThatThrownBy(() -> service.requestGeneration(generateRequest()))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("does not belong");
    }

    @Test
    void generationCreatesAssessmentInGeneratingStatusAndPublishesEvent() {
        when(currentUserProvider.getCurrentUser()).thenReturn(author);
        when(courseRepository.findById(3L)).thenReturn(Optional.of(course));
        when(documentRepository.findAllById(List.of(7L)))
                .thenReturn(List.of(document(7L, course, DocumentStatus.READY)));
        when(assessmentRepository.save(any(Assessment.class))).thenAnswer(inv -> withId(inv.getArgument(0), 10L));

        AssessmentSummaryResponse response = service.requestGeneration(generateRequest());

        assertThat(response.status()).isEqualTo(AssessmentStatus.GENERATING);
        assertThat(response.courseId()).isEqualTo(3L);
        verify(eventPublisher).publishEvent(any(AssessmentGenerationRequestedEvent.class));
    }

    @Test
    void onlyReadyAssessmentsCanBePublished() {
        assessment.setStatus(AssessmentStatus.GENERATING);
        when(assessmentRepository.findWithQuestionsById(10L)).thenReturn(Optional.of(assessment));
        when(currentUserProvider.getCurrentUser()).thenReturn(author);

        assertThatThrownBy(() -> service.publish(10L)).isInstanceOf(BadRequestException.class);
    }

    @Test
    void publishingMovesReadyAssessmentToPublished() {
        assessment.setStatus(AssessmentStatus.READY);
        assessment.addQuestion(question(100L));
        when(assessmentRepository.findWithQuestionsById(10L)).thenReturn(Optional.of(assessment));
        when(currentUserProvider.getCurrentUser()).thenReturn(author);

        assertThat(service.publish(10L).status()).isEqualTo(AssessmentStatus.PUBLISHED);
    }

    @Test
    void onlyTheAuthorCanSeeTheAssessmentWithAnswers() {
        when(assessmentRepository.findWithQuestionsById(10L)).thenReturn(Optional.of(assessment));
        when(currentUserProvider.getCurrentUser()).thenReturn(otherStudent);

        assertThatThrownBy(() -> service.findById(10L)).isInstanceOf(ForbiddenException.class);
    }

    @Test
    void questionsCannotBeEditedOnceTheAssessmentHasAttempts() {
        assessment.setStatus(AssessmentStatus.READY);
        assessment.addQuestion(question(100L));
        when(assessmentRepository.findWithQuestionsById(10L)).thenReturn(Optional.of(assessment));
        when(currentUserProvider.getCurrentUser()).thenReturn(author);
        when(attemptRepository.existsByAssessmentId(10L)).thenReturn(true);

        QuestionRequest request = new QuestionRequest("New text", QuestionType.TRUE_FALSE, Difficulty.EASY,
                "1NF", "Because", List.of(new OptionRequest("Verdadero", true), new OptionRequest("Falso", false)));

        assertThatThrownBy(() -> service.updateQuestion(10L, 100L, request))
                .isInstanceOf(DuplicateResourceException.class);
    }

    private AssessmentGenerateRequest generateRequest() {
        return new AssessmentGenerateRequest(3L, "Normalization practice", List.of(7L), 5,
                Difficulty.MEDIUM, Set.of(QuestionType.MULTIPLE_CHOICE), null);
    }

    private Document document(Long id, Course documentCourse, DocumentStatus status) {
        Document document = withId(new Document(), id);
        document.setCourse(documentCourse);
        document.setStatus(status);
        return document;
    }

    private Question question(Long id) {
        Question question = withId(new Question(), id);
        question.setText("What does 1NF require?");
        question.setType(QuestionType.TRUE_FALSE);
        question.setDifficulty(Difficulty.EASY);
        return question;
    }

    private static <T> T withId(T entity, Long id) {
        ReflectionTestUtils.setField(entity, "id", id);
        return entity;
    }
}