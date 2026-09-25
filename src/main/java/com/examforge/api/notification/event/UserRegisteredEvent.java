package com.examforge.api.notification.event;

public record UserRegisteredEvent(Long userId, String email, String fullName) {
}
