package com.kms.tripplanning.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.mailslurp.apis.EmailControllerApi;
import com.mailslurp.apis.InboxControllerApi;
import com.mailslurp.clients.ApiClient;
import com.mailslurp.clients.auth.ApiKeyAuth;


@Configuration
public class MailConfig {

    @Value("${spring.brevo.smtp-api-key}")
    private String apiKey;

    @Bean
    public ApiClient mailSlurpClient() {
        ApiClient client = new ApiClient();
        client.setApiKey(apiKey);
        return client;
    }

    @Bean
    public InboxControllerApi inboxApi(ApiClient client) {
        return new InboxControllerApi(client);
    }

    @Bean
    public EmailControllerApi emailApi(ApiClient client) {
        return new EmailControllerApi(client);
    }
}