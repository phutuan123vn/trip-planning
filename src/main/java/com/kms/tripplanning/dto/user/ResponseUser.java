package com.kms.tripplanning.dto.user;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;

public class ResponseUser {

    @AllArgsConstructor
    @Data
    public static class Me {
        private final UUID id;
        private final String firstName;
        private final String lastName;
        private final String email;
    }

    @AllArgsConstructor
    @Data
    public static class AuthResponse {
        private final String accessToken;
        private final String tokenType = "Bearer";
        private final UUID id;
        private final String email;
    }
}
