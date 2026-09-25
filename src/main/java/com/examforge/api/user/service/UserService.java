package com.examforge.api.user.service;

import com.examforge.api.common.dto.PageResponse;
import com.examforge.api.common.exception.ResourceNotFoundException;
import com.examforge.api.user.dto.UserResponse;
import com.examforge.api.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public UserResponse getByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(UserResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    @Transactional(readOnly = true)
    public PageResponse<UserResponse> findAll(Pageable pageable) {
        return PageResponse.from(userRepository.findAll(pageable).map(UserResponse::from));
    }
}
