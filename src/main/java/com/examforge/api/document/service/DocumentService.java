package com.examforge.api.document.service;

import com.examforge.api.academic.entity.Course;
import com.examforge.api.academic.repository.CourseRepository;
import com.examforge.api.common.exception.ResourceNotFoundException;
import com.examforge.api.document.dto.DocumentResponse;
import com.examforge.api.document.entity.Document;
import com.examforge.api.document.entity.DocumentStatus;
import com.examforge.api.document.event.DocumentUploadedEvent;
import com.examforge.api.document.mapper.DocumentMapper;
import com.examforge.api.document.repository.DocumentRepository;
import com.examforge.api.security.CurrentUserProvider;
import com.examforge.api.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final CourseRepository courseRepository;
    private final CurrentUserProvider currentUserProvider;
    private final DocumentMapper documentMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Value("${app.storage.upload-dir:./uploads}")
    private String uploadDir;

    @Transactional
    public DocumentResponse uploadDocument(Long courseId, String title, MultipartFile file) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));
        User currentUser = currentUserProvider.getCurrentUser();

        try {
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String filename = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            Path filePath = uploadPath.resolve(filename);
            Files.copy(file.getInputStream(), filePath);

            Document document = new Document();
            document.setTitle(title);
            document.setFileUrl(filePath.toString());
            document.setFileSize(file.getSize());
            document.setStatus(DocumentStatus.PENDING);
            document.setCourse(course);
            document.setUser(currentUser);

            Document savedDocument = documentRepository.save(document);

            eventPublisher.publishEvent(new DocumentUploadedEvent(savedDocument.getId()));

            return documentMapper.toResponse(savedDocument);
        } catch (IOException e) {
            throw new RuntimeException("Could not store file", e);
        }
    }
}
