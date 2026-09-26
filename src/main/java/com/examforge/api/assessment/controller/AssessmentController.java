package com.examforge.api.assessment.controller;

import java.util.List;

import com.examforge.api.assessment.dto.AssessmentDetailResponse;
import com.examforge.api.assessment.dto.AssessmentGenerateRequest;
import com.examforge.api.assessment.dto.AssessmentSummaryResponse;
import com.examforge.api.assessment.dto.AssessmentUpdateRequest;
import com.examforge.api.assessment.dto.QuestionRequest;
import com.examforge.api.assessment.dto.QuestionResponse;
import com.examforge.api.assessment.dto.QuestionSourceResponse;
import com.examforge.api.assessment.service.AssessmentService;
import com.examforge.api.common.dto.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Assessments", description = "RAG generation, review and publication of assessments")
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class AssessmentController {

    private final AssessmentService assessmentService;

    @Operation(summary = "Generate an assessment from course documents (async; poll GET /assessments/{id})")
    @PostMapping("/assessments/generate")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public AssessmentSummaryResponse generate(@Valid @RequestBody AssessmentGenerateRequest request) {
        return assessmentService.requestGeneration(request);
    }

    @Operation(summary = "List the assessments created by the current user")
    @GetMapping("/assessments/me")
    public PageResponse<AssessmentSummaryResponse> findMine(@PageableDefault(size = 20) Pageable pageable) {
        return assessmentService.findMine(pageable);
    }

    @Operation(summary = "List the published assessments of a course")
    @GetMapping("/courses/{courseId}/assessments")
    public PageResponse<AssessmentSummaryResponse> findPublishedByCourse(@PathVariable Long courseId,
                                                                        @PageableDefault(size = 20) Pageable pageable) {
        return assessmentService.findPublishedByCourse(courseId, pageable);
    }

    @Operation(summary = "Get an assessment with its questions and correct answers (author only)")
    @GetMapping("/assessments/{id}")
    public AssessmentDetailResponse findById(@PathVariable Long id) {
        return assessmentService.findById(id);
    }

    @Operation(summary = "Update title, description, difficulty and visibility")
    @PutMapping("/assessments/{id}")
    public AssessmentDetailResponse update(@PathVariable Long id, @Valid @RequestBody AssessmentUpdateRequest request) {
        return assessmentService.update(id, request);
    }

    @Operation(summary = "Publish a READY assessment so students can take it")
    @PostMapping("/assessments/{id}/publish")
    public AssessmentDetailResponse publish(@PathVariable Long id) {
        return assessmentService.publish(id);
    }

    @Operation(summary = "Delete an assessment without attempts")
    @DeleteMapping("/assessments/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        assessmentService.delete(id);
    }

    @Operation(summary = "Edit a generated question before publishing")
    @PutMapping("/assessments/{id}/questions/{questionId}")
    public QuestionResponse updateQuestion(@PathVariable Long id, @PathVariable Long questionId,
                                           @Valid @RequestBody QuestionRequest request) {
        return assessmentService.updateQuestion(id, questionId, request);
    }

    @Operation(summary = "Remove a question before publishing")
    @DeleteMapping("/assessments/{id}/questions/{questionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteQuestion(@PathVariable Long id, @PathVariable Long questionId) {
        assessmentService.deleteQuestion(id, questionId);
    }

    @Operation(summary = "Document fragments that support a question, most relevant first")
    @GetMapping("/assessments/{id}/questions/{questionId}/sources")
    public List<QuestionSourceResponse> findQuestionSources(@PathVariable Long id, @PathVariable Long questionId) {
        return assessmentService.findQuestionSources(id, questionId);
    }
}
