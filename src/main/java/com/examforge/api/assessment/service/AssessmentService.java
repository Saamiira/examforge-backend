package com.examforge.api.assessment.service;

import java.util.EnumSet;
import java.util.List;

import com.examforge.api.academic.entity.Course;
import com.examforge.api.academic.repository.CourseRepository;
import com.examforge.api.academic.repository.UserCourseRepository;
import com.examforge.api.assessment.dto.AssessmentDetailResponse;
import com.examforge.api.assessment.dto.AssessmentGenerateRequest;
import com.examforge.api.assessment.dto.AssessmentSummaryResponse;
import com.examforge.api.assessment.dto.AssessmentUpdateRequest;
import com.examforge.api.assessment.dto.QuestionRequest;
import com.examforge.api.assessment.dto.QuestionResponse;
import com.examforge.api.assessment.dto.QuestionSourceResponse;
import com.examforge.api.assessment.entity.Assessment;
import com.examforge.api.assessment.entity.AssessmentStatus;
import com.examforge.api.assessment.entity.Question;
import com.examforge.api.assessment.entity.QuestionType;
import com.examforge.api.assessment.entity.Visibility;
import com.examforge.api.assessment.generation.AssessmentGenerationRequestedEvent;
import com.examforge.api.assessment.mapper.AssessmentMapper;
import com.examforge.api.assessment.mapper.QuestionMapper;
import com.examforge.api.assessment.repository.AssessmentRepository;
import com.examforge.api.assessment.repository.QuestionSourceRepository;
import com.examforge.api.attempt.repository.AttemptRepository;
import com.examforge.api.common.dto.PageResponse;
import com.examforge.api.common.exception.BadRequestException;
import com.examforge.api.common.exception.DuplicateResourceException;
import com.examforge.api.common.exception.ForbiddenException;
import com.examforge.api.common.exception.ResourceNotFoundException;
import com.examforge.api.document.entity.Document;
import com.examforge.api.document.entity.DocumentStatus;
import com.examforge.api.document.repository.DocumentRepository;
import com.examforge.api.security.CurrentUserProvider;
import com.examforge.api.user.entity.Role;
import com.examforge.api.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AssessmentService {

    private static final int TRUE_FALSE_OPTIONS = 2;
    private static final int MIN_CHOICE_OPTIONS = 3;

    private final AssessmentRepository assessmentRepository;
    private final QuestionSourceRepository questionSourceRepository;
    private final CourseRepository courseRepository;
    private final DocumentRepository documentRepository;
    private final AttemptRepository attemptRepository;
    private final CurrentUserProvider currentUserProvider;
    private final AssessmentMapper assessmentMapper;
    private final QuestionMapper questionMapper;
    private final ApplicationEventPublisher eventPublisher;
    private final UserCourseRepository userCourseRepository;

    @Transactional
    public AssessmentSummaryResponse requestGeneration(AssessmentGenerateRequest request) {
        User author = currentUserProvider.getCurrentUser();
        Course course = courseRepository.findById(request.courseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course", request.courseId()));
        if (author.getRole() == Role.STUDENT
                && !userCourseRepository.existsByUserIdAndCourseId(author.getId(), course.getId())) {
            throw new ForbiddenException("Enroll in the course to generate assessments from its documents");
        }
        List<Long> documentIds = request.documentIds().stream().distinct().toList();
        validateDocuments(documentIds, course.getId());

        Assessment assessment = new Assessment();
        assessment.setAuthor(author);
        assessment.setCourse(course);
        assessment.setTitle(request.title());
        assessment.setDifficulty(request.difficulty());
        assessment.setStatus(AssessmentStatus.GENERATING);
        assessment.setVisibility(Visibility.PRIVATE);
        assessmentRepository.save(assessment);

        eventPublisher.publishEvent(new AssessmentGenerationRequestedEvent(assessment.getId(), documentIds, request));
        return assessmentMapper.toSummary(assessment);
    }

    @Transactional(readOnly = true)
    public AssessmentDetailResponse findById(Long id) {
        return assessmentMapper.toDetail(findOwned(id));
    }

    @Transactional(readOnly = true)
    public PageResponse<AssessmentSummaryResponse> findMine(Pageable pageable) {
        Long authorId = currentUserProvider.getCurrentUserId();
        return PageResponse.from(assessmentRepository.findByAuthorIdOrderByCreatedAtDesc(authorId, pageable)
                .map(assessmentMapper::toSummary));
    }

    @Transactional(readOnly = true)
    public PageResponse<AssessmentSummaryResponse> findPublishedByCourse(Long courseId, Pageable pageable) {
        if (!courseRepository.existsById(courseId)) {
            throw new ResourceNotFoundException("Course", courseId);
        }
        return PageResponse.from(assessmentRepository.findByCourseIdAndStatusAndVisibilityInOrderByCreatedAtDesc(
                        courseId, AssessmentStatus.PUBLISHED, EnumSet.of(Visibility.PUBLIC, Visibility.COURSE), pageable)
                .map(assessmentMapper::toSummary));
    }

    @Transactional
    public AssessmentDetailResponse update(Long id, AssessmentUpdateRequest request) {
        Assessment assessment = findOwned(id);
        ensureNotGenerating(assessment);
        assessment.setTitle(request.title());
        assessment.setDescription(request.description());
        assessment.setDifficulty(request.difficulty());
        assessment.setVisibility(request.visibility());
        return assessmentMapper.toDetail(assessment);
    }

    @Transactional
    public AssessmentDetailResponse publish(Long id) {
        Assessment assessment = findOwned(id);
        if (assessment.getStatus() != AssessmentStatus.READY) {
            throw new BadRequestException("Only READY assessments can be published (current: " + assessment.getStatus() + ")");
        }
        if (assessment.getQuestions().isEmpty()) {
            throw new BadRequestException("An assessment without questions cannot be published");
        }
        assessment.setStatus(AssessmentStatus.PUBLISHED);
        return assessmentMapper.toDetail(assessment);
    }

    @Transactional
    public void delete(Long id) {
        Assessment assessment = findOwned(id);
        ensureNotGenerating(assessment);
        ensureNoAttempts(assessment);
        assessmentRepository.delete(assessment);
    }

    @Transactional
    public QuestionResponse updateQuestion(Long assessmentId, Long questionId, QuestionRequest request) {
        Assessment assessment = findEditable(assessmentId);
        validateOptions(request);
        Question question = findQuestion(assessment, questionId);
        question.setText(request.text());
        question.setType(request.type());
        question.setDifficulty(request.difficulty());
        question.setTopic(request.topic());
        question.setExplanation(request.explanation());
        question.replaceOptions(questionMapper.toOptionEntities(request.options()));
        assessmentRepository.flush();
        return questionMapper.toResponse(question);
    }

    @Transactional
    public void deleteQuestion(Long assessmentId, Long questionId) {
        Assessment assessment = findEditable(assessmentId);
        Question question = findQuestion(assessment, questionId);
        assessment.removeQuestion(question);
    }

    @Transactional(readOnly = true)
    public List<QuestionSourceResponse> findQuestionSources(Long assessmentId, Long questionId) {
        Assessment assessment = findOwned(assessmentId);
        findQuestion(assessment, questionId);
        return questionSourceRepository.findByQuestionIdOrderByRelevanceScoreDesc(questionId).stream()
                .map(assessmentMapper::toSourceResponse)
                .toList();
    }

    private void validateDocuments(List<Long> documentIds, Long courseId) {
        List<Document> documents = documentRepository.findAllById(documentIds);
        if (documents.size() != documentIds.size()) {
            throw new ResourceNotFoundException("One or more documents do not exist");
        }
        for (Document document : documents) {
            if (!document.getCourse().getId().equals(courseId)) {
                throw new BadRequestException("Document " + document.getId() + " does not belong to course " + courseId);
            }
            if (document.getStatus() != DocumentStatus.READY) {
                throw new BadRequestException("Document " + document.getId() + " is not ready (status: "
                        + document.getStatus() + ")");
            }
        }
    }

    private Assessment findOwned(Long id) {
        Assessment assessment = assessmentRepository.findWithQuestionsById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Assessment", id));
        User user = currentUserProvider.getCurrentUser();
        if (!assessment.isAuthoredBy(user.getId()) && user.getRole() != Role.ADMIN) {
            throw new ForbiddenException("Only the author can manage this assessment");
        }
        return assessment;
    }

    private Assessment findEditable(Long id) {
        Assessment assessment = findOwned(id);
        if (assessment.getStatus() != AssessmentStatus.READY && assessment.getStatus() != AssessmentStatus.DRAFT) {
            throw new BadRequestException("Questions can only be edited before publishing (current: "
                    + assessment.getStatus() + ")");
        }
        ensureNoAttempts(assessment);
        return assessment;
    }

    private Question findQuestion(Assessment assessment, Long questionId) {
        return assessment.getQuestions().stream()
                .filter(question -> question.getId().equals(questionId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Question", questionId));
    }

    private void ensureNotGenerating(Assessment assessment) {
        if (assessment.getStatus() == AssessmentStatus.GENERATING) {
            throw new BadRequestException("The assessment is still being generated");
        }
    }

    private void ensureNoAttempts(Assessment assessment) {
        if (attemptRepository.existsByAssessmentId(assessment.getId())) {
            throw new DuplicateResourceException("The assessment already has attempts and cannot be modified");
        }
    }

    private void validateOptions(QuestionRequest request) {
        long correct = request.options().stream().filter(option -> option.correct()).count();
        if (correct != 1) {
            throw new BadRequestException("A question must have exactly one correct option");
        }
        int size = request.options().size();
        if (request.type() == QuestionType.TRUE_FALSE && size != TRUE_FALSE_OPTIONS) {
            throw new BadRequestException("TRUE_FALSE questions must have exactly 2 options");
        }
        if (request.type() == QuestionType.MULTIPLE_CHOICE && size < MIN_CHOICE_OPTIONS) {
            throw new BadRequestException("MULTIPLE_CHOICE questions need at least 3 options");
        }
    }
}