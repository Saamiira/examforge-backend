package com.examforge.api.document.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DocumentUploadedEvent {
    private final Long documentId;
}
