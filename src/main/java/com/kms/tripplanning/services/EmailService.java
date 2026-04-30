package com.kms.tripplanning.services;

public interface EmailService {
    void sendVerificationEmail(String to, String token);
}
