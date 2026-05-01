package com.kms.tripplanning.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import brevo.ApiClient;
import brevoApi.TransactionalEmailsApi;

@Configuration
public class MailConfig {

    @Value("${spring.brevo.smtp-api-key}")
    private String apiKey;

    @Bean
    public TransactionalEmailsApi transactionalEmailsApi() {
        ApiClient defaultClient = brevo.Configuration.getDefaultApiClient();
        defaultClient.setApiKey(apiKey);
        return new TransactionalEmailsApi(defaultClient);
    }
}