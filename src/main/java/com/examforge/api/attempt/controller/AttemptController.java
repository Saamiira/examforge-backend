package com.examforge.api.attempt.controller;

import com.examforge.api.attempt.dto.AttemptResultResponse;
import com.examforge.api.attempt.dto.AttemptStartResponse;
import com.examforge.api.attempt.dto.AttemptSubmitRequest;
import com.examforge.api.attempt.dto.AttemptSummaryResponse;
import com.examforge.api.attempt.service.AttemptService;
import com.examforge.api.common.dto.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Attempts", description = "Take assessments, submit answers and review results")
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class AttemptController {

    private final AttemptService attemptService;

    @Operation(summary = "Start an attempt on an assessment (correct answers are hidden)")
    @PostMapping("/assessments/{assessmentId}/attempts")
    @ResponseStatus(HttpStatus.CREATED)
    public AttemptStartResponse start(@PathVariable Long assessmentId) {
        return attemptService.start(assessmentId);
    }

    @Operation(summary = "Submit the answers of an attempt and get the graded result")
    @PostMapping("/attempts/{attemptId}/submit")
    public AttemptResultResponse submit(@PathVariable Long attemptId,
                                        @Valid @RequestBody AttemptSubmitRequest request) {
        return attemptService.submit(attemptId, request);
    }

    @Operation(summary = "Get the result and feedback of a submitted attempt")
    @GetMapping("/attempts/{attemptId}")
    public AttemptResultResponse getResult(@PathVariable Long attemptId) {
        return attemptService.getResult(attemptId);
    }

    @Operation(summary = "List the current user's attempts, newest first")
    @GetMapping("/attempts/me")
    public PageResponse<AttemptSummaryResponse> findMine(@PageableDefault(size = 20) Pageable pageable) {
        return attemptService.findMine(pageable);
    }
}
