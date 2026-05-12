package com.kms.tripplanning.listener;

import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.kms.tripplanning.event.UserCreatedEvent;
import com.kms.tripplanning.services.EmailService;

@ExtendWith(MockitoExtension.class)
class UserCreatedListenerTest {

    @Mock
    private EmailService emailService;

    @InjectMocks
    private UserCreatedListener listener;

    @BeforeEach
    void setUp() {
    }

    @Test
    void handleUserCreatedEvent_shouldSendVerificationEmail() {
        String email = "test@example.com";
        String token = "verification-token";
        String userId = "user-id";
        UserCreatedEvent event = new UserCreatedEvent(userId, email, token);

        listener.handleUserCreatedEvent(event);

        verify(emailService).sendVerificationEmail(email, token);
    }
}
