package com.examforge.api.user.service;

import java.util.Locale;

import com.examforge.api.user.entity.Role;
import com.examforge.api.user.entity.User;
import com.examforge.api.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Creates the first ADMIN from ADMIN_EMAIL / ADMIN_PASSWORD on startup.
 * Does nothing when the variables are empty or the account already exists.
 */
@Slf4j
@Component
public class AdminInitializer implements ApplicationRunner {

    private static final int MIN_PASSWORD_LENGTH = 8;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final String email;
    private final String password;

    public AdminInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder,
                            @Value("${app.admin.email}") String email,
                            @Value("${app.admin.password}") String password) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.email = email.trim().toLowerCase(Locale.ROOT);
        this.password = password;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (email.isEmpty() || password.isBlank()) {
            log.info("ADMIN_EMAIL/ADMIN_PASSWORD not set; skipping initial admin creation");
            return;
        }
        if (password.length() < MIN_PASSWORD_LENGTH) {
            log.warn("ADMIN_PASSWORD must have at least {} characters; skipping initial admin creation",
                    MIN_PASSWORD_LENGTH);
            return;
        }
        if (userRepository.existsByEmail(email)) {
            log.info("Initial admin {} already exists", email);
            return;
        }

        userRepository.save(new User("Administrator", email, passwordEncoder.encode(password), Role.ADMIN));
        log.info("Initial admin {} created", email);
    }
}
