package com.examforge.api.notification;

import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.after;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

import java.util.UUID;

import com.examforge.api.notification.event.AssessmentGeneratedEvent;
import com.examforge.api.notification.event.AttemptCompletedEvent;
import com.examforge.api.notification.event.UserRegisteredEvent;
import com.examforge.api.notification.service.EmailService;
import com.examforge.api.user.entity.Role;
import com.examforge.api.user.entity.User;
import com.examforge.api.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.support.TransactionTemplate;

@SpringBootTest
class NotificationEventListenerTest {

    @MockBean
    private EmailService emailService;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private UserRepository userRepository;

    private User user;

    @BeforeEach
    void setUp() {
        String email = "notify-" + UUID.randomUUID() + "@example.com";
        user = userRepository.save(new User("Notify Test", email, "not-used", Role.STUDENT));
    }

    @AfterEach
    void tearDown() {
        userRepository.delete(user);
    }

    @Test
    void sendsWelcomeEmailAfterCommit() throws Exception {
        transactionTemplate.executeWithoutResult(status -> eventPublisher.publishEvent(
                new UserRegisteredEvent(user.getId(), user.getEmail(), user.getFullName())));

        verify(emailService, timeout(5000)).sendHtml(eq(user.getEmail()), anyString(), eq("welcome"), anyMap());
    }

    @Test
    void sendsAttemptCompletedEmailToAttemptOwner() throws Exception {
        transactionTemplate.executeWithoutResult(status ->
                eventPublisher.publishEvent(new AttemptCompletedEvent(10L, user.getId())));

        verify(emailService, timeout(5000))
                .sendHtml(eq(user.getEmail()), anyString(), eq("attempt-completed"), anyMap());
    }

    @Test
    void sendsAssessmentGeneratedEmailToOwner() throws Exception {
        transactionTemplate.executeWithoutResult(status ->
                eventPublisher.publishEvent(new AssessmentGeneratedEvent(20L, user.getId())));

        verify(emailService, timeout(5000))
                .sendHtml(eq(user.getEmail()), anyString(), eq("assessment-generated"), anyMap());
    }

    @Test
    void doesNotSendEmailWhenTransactionRollsBack() throws Exception {
        transactionTemplate.executeWithoutResult(status -> {
            eventPublisher.publishEvent(new UserRegisteredEvent(user.getId(), user.getEmail(), user.getFullName()));
            status.setRollbackOnly();
        });

        verify(emailService, after(1000).never()).sendHtml(anyString(), anyString(), anyString(), anyMap());
    }
}
