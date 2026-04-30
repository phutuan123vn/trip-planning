package com.kms.tripplanning.services.Impl;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.kms.tripplanning.services.EmailService;
import com.mailslurp.apis.EmailControllerApi;
import com.mailslurp.apis.InboxControllerApi;
import com.mailslurp.models.SendEmailOptions;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final InboxControllerApi inboxApi;
    private final EmailControllerApi emailApi;

    @Override
    public void sendVerificationEmail(String to, String token) {

        // Create and send email using MailSlurp API
        String subject = "Verify your email";
        String body = "Please click the following link to verify your email: " +
                "http://localhost:8080/api/auth/verify?token=" + token;

        
    }

}