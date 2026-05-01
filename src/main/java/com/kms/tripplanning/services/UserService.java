package com.kms.tripplanning.services;

import org.springframework.security.core.userdetails.UserDetailsService;

import com.kms.tripplanning.dto.user.RequestUser;
import com.kms.tripplanning.dto.user.ResponseUser;

public interface UserService extends UserDetailsService {

    ResponseUser.AuthResponse login(RequestUser.Login request);

    void registerUser(RequestUser.Register request);

    ResponseUser.Me getCurrentUser();

    void verifyEmail(RequestUser.VerifyEmail request);

    
}
