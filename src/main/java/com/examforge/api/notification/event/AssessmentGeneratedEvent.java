package com.examforge.api.notification.event;

public record AssessmentGeneratedEvent(Long assessmentId, Long ownerId) {
}
