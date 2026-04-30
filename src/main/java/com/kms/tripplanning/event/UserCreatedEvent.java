package com.kms.tripplanning.event;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserCreatedEvent {
    private final String userId;
    private final String email;
    private final String token;
}
