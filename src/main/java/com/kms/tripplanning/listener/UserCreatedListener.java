package com.kms.tripplanning.listener;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.kms.tripplanning.event.UserCreatedEvent;
import com.kms.tripplanning.services.EmailService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UserCreatedListener {

    private final EmailService emailService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleUserCreatedEvent(UserCreatedEvent event) {
        // Send verification email
        emailService.sendVerificationEmail(event.getEmail(), event.getToken());
    }
}
