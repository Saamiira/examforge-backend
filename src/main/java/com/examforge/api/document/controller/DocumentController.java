package com.examforge.api.document.controller;

import com.examforge.api.document.dto.DocumentResponse;
import com.examforge.api.document.service.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/courses/{courseId}/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

    @PostMapping
    @PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
    public ResponseEntity<DocumentResponse> uploadDocument(
            @PathVariable Long courseId,
            @RequestParam("title") String title,
            @RequestParam("file") MultipartFile file) {
            
        DocumentResponse response = documentService.uploadDocument(courseId, title, file);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    @GetMapping
    public List<DocumentResponse> findByCourse(@PathVariable Long courseId) {
        return documentService.findByCourse(courseId);
    }

    @GetMapping("/{documentId}")
    public DocumentResponse findById(@PathVariable Long courseId, @PathVariable Long documentId) {
        return documentService.findById(courseId, documentId);
    }
}
