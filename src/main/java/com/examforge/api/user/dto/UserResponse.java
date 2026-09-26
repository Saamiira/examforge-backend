package com.examforge.api.user.dto;

import java.time.Instant;

import com.examforge.api.user.entity.Role;
import com.examforge.api.user.entity.User;

public record UserResponse(
        Long id,
        String fullName,
        String email,
        Role role,
        Instant createdAt
) {

    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getFullName(), user.getEmail(), user.getRole(),
                user.getCreatedAt());
    }
}
