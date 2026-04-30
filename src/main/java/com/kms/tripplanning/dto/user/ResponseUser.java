package com.kms.tripplanning.dto.user;

import lombok.AllArgsConstructor;
import lombok.Data;

public class ResponseUser {

    @AllArgsConstructor
    @Data
    public static class Me {
        private final String id;
        private final String firstName;
        private final String lastName;
        private final String email;
    }

    @AllArgsConstructor
    @Data
    public static class AuthResponse {
        private final String accessToken;
        private final String tokenType = "Bearer";
        private final String id;
        private final String email;
    }
}
