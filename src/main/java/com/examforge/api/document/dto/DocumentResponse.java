package com.examforge.api.document.dto;

import com.examforge.api.document.entity.DocumentStatus;
import lombok.Data;

@Data
public class DocumentResponse {
    private Long id;
    private String title;
    private String fileUrl;
    private Long fileSize;
    private DocumentStatus status;
    private String errorMessage;
    private Long courseId;
    private Long userId;
}
