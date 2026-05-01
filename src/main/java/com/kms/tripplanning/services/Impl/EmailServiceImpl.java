package com.kms.tripplanning.services.Impl;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.kms.tripplanning.services.EmailService;

import brevoApi.TransactionalEmailsApi;
import brevoModel.SendSmtpEmail;
import brevoModel.SendSmtpEmailSender;
import brevoModel.SendSmtpEmailTo;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final Logger logger = LoggerFactory.getLogger(EmailServiceImpl.class);

    private final TransactionalEmailsApi apiInstance;

    @Override
    public void sendVerificationEmail(String to, String token) {
        SendSmtpEmail email = createVerifyEmail(to, token);
        email.setSender(getSender());
        try {
            apiInstance.sendTransacEmail(email);
        } catch (Exception e) {
            logger.error("Failed to send verification email to {}: {}", to, e.getMessage());
            e.printStackTrace();
        }
    }

    private SendSmtpEmail createVerifyEmail(String to, String token) {
        SendSmtpEmail email = new SendSmtpEmail();
        email.setTo(getRecipients(to));
        email.setSubject("Verify your email");
        email.setHtmlContent("<html><body><p>Click the link below to verify your email:</p>" +
                "<a href=\"http://localhost:8080/api/auth/verify?token=" + token + "\">Verify Email</a>" +
                "</body></html>");
        return email;
    }

    private SendSmtpEmailSender getSender() {
        SendSmtpEmailSender sender = new SendSmtpEmailSender();
        sender.setName("Trip Planning App");
        sender.setEmail("debr123vn@gmail.com");
        return sender;
    }

    private List<SendSmtpEmailTo> getRecipients(String... to) {
        return List.of(to).stream().map((t) -> {
            SendSmtpEmailTo recipient = new SendSmtpEmailTo();
            recipient.setEmail(t);
            return recipient;
        }).toList();
    }

}