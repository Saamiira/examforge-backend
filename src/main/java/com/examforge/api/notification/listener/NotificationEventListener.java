package com.examforge.api.notification.listener;

import java.util.Map;

import com.examforge.api.notification.event.AssessmentGeneratedEvent;
import com.examforge.api.notification.event.AttemptCompletedEvent;
import com.examforge.api.notification.event.UserRegisteredEvent;
import com.examforge.api.notification.service.EmailService;
import com.examforge.api.user.entity.User;
import com.examforge.api.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Sends notification emails off the request thread, only after the publishing transaction commits.
 * Events must be published inside a @Transactional method; otherwise they are not delivered.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventListener {

    private final EmailService emailService;
    private final UserRepository userRepository;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onUserRegistered(UserRegisteredEvent event) {
        try {
            emailService.sendHtml(event.email(), "Bienvenido a ExamForge", "welcome",
                    Map.of("fullName", event.fullName()));
            log.info("Welcome email sent to user {}", event.userId());
        } catch (Exception ex) {
            log.error("Failed to send welcome email to user {}", event.userId(), ex);
        }
    }

    // TODO: load the Attempt by id (score, performance by topic) once the attempt module is in develop.
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onAttemptCompleted(AttemptCompletedEvent event) {
        try {
            User user = findUser(event.userId());
            if (user == null) {
                return;
            }
            emailService.sendHtml(user.getEmail(), "Tus resultados en ExamForge", "attempt-completed",
                    Map.of("fullName", user.getFullName(), "attemptId", event.attemptId()));
            log.info("Attempt completed email sent for attempt {}", event.attemptId());
        } catch (Exception ex) {
            log.error("Failed to send attempt completed email for attempt {}", event.attemptId(), ex);
        }
    }

    // TODO: load the Assessment by id (title, course, question count) once the assessment module is in develop.
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onAssessmentGenerated(AssessmentGeneratedEvent event) {
        try {
            User owner = findUser(event.ownerId());
            if (owner == null) {
                return;
            }
            emailService.sendHtml(owner.getEmail(), "Tu evaluación está lista", "assessment-generated",
                    Map.of("fullName", owner.getFullName(), "assessmentId", event.assessmentId()));
            log.info("Assessment generated email sent for assessment {}", event.assessmentId());
        } catch (Exception ex) {
            log.error("Failed to send assessment generated email for assessment {}", event.assessmentId(), ex);
        }
    }

    private User findUser(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            log.warn("Skipping notification: user {} not found", userId);
        }
        return user;
    }
}
