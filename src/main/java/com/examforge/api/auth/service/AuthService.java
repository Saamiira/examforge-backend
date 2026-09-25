package com.examforge.api.auth.service;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;

import com.examforge.api.auth.dto.AuthResponse;
import com.examforge.api.auth.dto.LoginRequest;
import com.examforge.api.auth.dto.RefreshTokenRequest;
import com.examforge.api.auth.dto.RegisterRequest;
import com.examforge.api.auth.entity.RefreshToken;
import com.examforge.api.auth.repository.RefreshTokenRepository;
import com.examforge.api.common.exception.DuplicateResourceException;
import com.examforge.api.common.exception.InvalidTokenException;
import com.examforge.api.common.exception.UnauthorizedException;
import com.examforge.api.notification.event.UserRegisteredEvent;
import com.examforge.api.security.JwtService;
import com.examforge.api.user.dto.UserResponse;
import com.examforge.api.user.entity.Role;
import com.examforge.api.user.entity.User;
import com.examforge.api.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final String INVALID_CREDENTIALS = "Invalid email or password";
    private static final SecureRandom RANDOM = new SecureRandom();

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final ApplicationEventPublisher eventPublisher;

    @Value("${app.jwt.refresh-expiration-ms}")
    private long refreshExpirationMs;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = request.email();
        if (userRepository.existsByEmail(email)) {
            throw new DuplicateResourceException("Email is already registered");
        }

        User user = userRepository.save(new User(request.fullName().trim(), email,
                passwordEncoder.encode(request.password()), Role.STUDENT));
        eventPublisher.publishEvent(new UserRegisteredEvent(user.getId(), user.getEmail(), user.getFullName()));
        return issueTokens(user);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        String email = request.email();
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, request.password()));
        } catch (AuthenticationException ex) {
            throw new UnauthorizedException(INVALID_CREDENTIALS);
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException(INVALID_CREDENTIALS));
        return issueTokens(user);
    }

    @Transactional
    public AuthResponse refresh(RefreshTokenRequest request) {
        RefreshToken current = refreshTokenRepository.findByToken(request.refreshToken())
                .orElseThrow(() -> new InvalidTokenException("Refresh token not found"));
        if (current.isRevoked() || current.isExpired()) {
            throw new InvalidTokenException("Refresh token is expired or revoked");
        }

        current.setRevoked(true);
        return issueTokens(current.getUser());
    }

    @Transactional
    public void logout(RefreshTokenRequest request) {
        refreshTokenRepository.findByToken(request.refreshToken())
                .ifPresent(token -> token.setRevoked(true));
    }

    private AuthResponse issueTokens(User user) {
        RefreshToken refreshToken = refreshTokenRepository.save(new RefreshToken(generateOpaqueToken(),
                Instant.now().plusMillis(refreshExpirationMs), user));

        return new AuthResponse(jwtService.generateAccessToken(user), refreshToken.getToken(), "Bearer",
                jwtService.getExpirationMs() / 1000, UserResponse.from(user));
    }

    private static String generateOpaqueToken() {
        byte[] bytes = new byte[48];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
