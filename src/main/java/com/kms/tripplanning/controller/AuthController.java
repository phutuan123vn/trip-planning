package com.kms.tripplanning.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.kms.tripplanning.dto.user.RequestUser;
import com.kms.tripplanning.dto.user.ResponseUser;
import com.kms.tripplanning.services.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@ApiController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void register(@Valid @RequestBody RequestUser.Register request) {
        userService.registerUser(request);
    }

    @PostMapping("/login")
    public ResponseUser.AuthResponse login(@Valid @RequestBody RequestUser.Login request) {
        return userService.login(request);
    }

    @GetMapping("/me")
    public ResponseUser.Me getCurrentUser() {
        return userService.getCurrentUser();
    }

}
