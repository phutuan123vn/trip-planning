package com.kms.tripplanning.services.Impl;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import brevoApi.TransactionalEmailsApi;
import brevoModel.SendSmtpEmail;

@ExtendWith(MockitoExtension.class)
class EmailServiceImplTest {

    @Mock
    private TransactionalEmailsApi apiInstance;

    @InjectMocks
    private EmailServiceImpl emailService;

    @Test
    void sendVerificationEmail_shouldCallSendTransacEmail() throws Exception {
        emailService.sendVerificationEmail("user@test.com", "token123");

        verify(apiInstance).sendTransacEmail(any(SendSmtpEmail.class));
    }

    @Test
    void sendVerificationEmail_shouldSetCorrectSender() throws Exception {
        ArgumentCaptor<SendSmtpEmail> captor = ArgumentCaptor.forClass(SendSmtpEmail.class);

        emailService.sendVerificationEmail("user@test.com", "token123");

        verify(apiInstance).sendTransacEmail(captor.capture());
        SendSmtpEmail email = captor.getValue();
        assertThat(email.getSender().getName()).isEqualTo("Trip Planning App");
        assertThat(email.getSender().getEmail()).isEqualTo("debr123vn@gmail.com");
    }

    @Test
    void sendVerificationEmail_shouldContainTokenInHtmlContent() throws Exception {
        ArgumentCaptor<SendSmtpEmail> captor = ArgumentCaptor.forClass(SendSmtpEmail.class);

        emailService.sendVerificationEmail("user@test.com", "my-verify-token");

        verify(apiInstance).sendTransacEmail(captor.capture());
        assertThat(captor.getValue().getHtmlContent()).contains("my-verify-token");
    }

    @Test
    void sendVerificationEmail_shouldSetCorrectRecipient() throws Exception {
        ArgumentCaptor<SendSmtpEmail> captor = ArgumentCaptor.forClass(SendSmtpEmail.class);

        emailService.sendVerificationEmail("recipient@test.com", "token");

        verify(apiInstance).sendTransacEmail(captor.capture());
        assertThat(captor.getValue().getTo()).hasSize(1);
        assertThat(captor.getValue().getTo().get(0).getEmail()).isEqualTo("recipient@test.com");
    }

    @Test
    void sendVerificationEmail_shouldHandleApiException_gracefully() throws Exception {
        doThrow(new RuntimeException("API error")).when(apiInstance).sendTransacEmail(any());

        // Should not throw
        assertThatCode(() -> emailService.sendVerificationEmail("user@test.com", "token"))
                .doesNotThrowAnyException();
    }
}
