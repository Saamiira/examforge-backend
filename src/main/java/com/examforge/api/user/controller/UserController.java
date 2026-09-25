package com.examforge.api.user.controller;

import com.examforge.api.common.dto.PageResponse;
import com.examforge.api.user.dto.UserResponse;
import com.examforge.api.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public UserResponse me(Authentication authentication) {
        return userService.getByEmail(authentication.getName());
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public PageResponse<UserResponse> findAll(@PageableDefault(size = 20) Pageable pageable) {
        return userService.findAll(pageable);
    }
}
