package com.kms.tripplanning.services.Impl;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kms.tripplanning.config.AuthUserDetails;
import com.kms.tripplanning.dto.user.RequestUser;
import com.kms.tripplanning.dto.user.ResponseUser;
import com.kms.tripplanning.entity.User;
import com.kms.tripplanning.event.UserCreatedEvent;
import com.kms.tripplanning.repository.UserRepository;
import com.kms.tripplanning.services.JwtService;
import com.kms.tripplanning.services.UserService;
import com.kms.tripplanning.utils.FilterBuilderHelper;
import com.kms.tripplanning.utils.SecurityUtils;

import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final ObjectProvider<AuthenticationManager> authenticationManagerProvider;

    private final @Lazy JwtService jwtService;

    private final ApplicationEventPublisher eventPublisher;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        FilterBuilderHelper filterBuilder = new FilterBuilderHelper();
        filterBuilder.addFilter("email", username);
        Page<User> users = userRepository.search(filterBuilder.build(), PageRequest.of(0, 10));
        var user = users.stream().findFirst()
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + username));
        return new AuthUserDetails(user);
    }

    @Override
    @Transactional
    public void registerUser(RequestUser.Register request) {
        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .build();
        userRepository.save(user);
        eventPublisher.publishEvent(new UserCreatedEvent(user.getId(), user.getEmail(),
                jwtService.generateVerifyToken(new AuthUserDetails(user))));
    }

    @Override
    public ResponseUser.Me getCurrentUser() {
        AuthUserDetails authUserDetails = SecurityUtils.getCurrentUser();
        return new ResponseUser.Me(authUserDetails.getId(), authUserDetails.getFirstName(),
                authUserDetails.getLastName(), authUserDetails.getUsername());
    }

    @Override
    public ResponseUser.AuthResponse login(RequestUser.Login request) {
        Authentication authentication = authenticationManagerProvider.getObject().authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        var authUserDetails = (AuthUserDetails) authentication.getPrincipal();
        return new ResponseUser.AuthResponse(jwtService.generateToken(authUserDetails), authUserDetails.getId(),
                authUserDetails.getUsername());
    }

    @Override
    public void verifyEmail(RequestUser.VerifyEmail request) {
        try {
            if (!jwtService.isTokenValid(request.getToken())) {
                throw new ValidationException("Invalid or expired token");
            }
        } catch (Exception e) {
            throw new ValidationException("Invalid token: " + e.getMessage());
        }

        try {
            boolean hasClaimVerifyEmail = jwtService.getClaimFromToken(request.getToken(),
                    (claims) -> claims.get("isVerifyEmail", Boolean.class));

            if (!hasClaimVerifyEmail)
                throw new ValidationException("Token does not contain required claim: isVerifyEmail");

        } catch (Exception e) {
            throw new ValidationException("Token does not contain required claims: " + e.getMessage());
        }

        AuthUserDetails credential = jwtService.getCredentialFromToken(request.getToken());

        if (!credential.getUsername().equals(request.getEmail())) {
            throw new ValidationException("Token does not match the provided email");
        }

        User user = userRepository.findById(credential.getId())
                .orElseThrow(() -> new ValidationException("User not found"));

        user.setActive(true);
        userRepository.save(user);
    }

}
