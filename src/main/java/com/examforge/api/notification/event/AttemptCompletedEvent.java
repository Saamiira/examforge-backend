package com.examforge.api.notification.event;

public record AttemptCompletedEvent(Long attemptId, Long userId) {
}
