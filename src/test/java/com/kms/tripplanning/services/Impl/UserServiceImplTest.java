package com.kms.tripplanning.services.Impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.kms.tripplanning.config.AuthUserDetails;
import com.kms.tripplanning.dto.user.RequestUser;
import com.kms.tripplanning.dto.user.ResponseUser;
import com.kms.tripplanning.entity.User;
import com.kms.tripplanning.event.UserCreatedEvent;
import com.kms.tripplanning.repository.UserRepository;
import com.kms.tripplanning.services.JwtService;
import com.kms.tripplanning.utils.SecurityUtils;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private ObjectProvider<AuthenticationManager> authenticationManagerProvider;

    @Mock
    private JwtService jwtService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private UserServiceImpl userService;

    private User sampleUser;
    private UUID userId;
    private MockedStatic<SecurityUtils> securityUtilsMock;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        sampleUser = User.builder()
                .id(userId)
                .email("test@example.com")
                .password("encodedPassword")
                .firstName("John")
                .lastName("Doe")
                .isActive(true)
                .isLocked(false)
                .build();
    }

    @AfterEach
    void tearDown() {
        if (securityUtilsMock != null) {
            securityUtilsMock.close();
        }
    }

    // ========== loadUserByUsername ==========

    @Test
    void loadUserByUsername_shouldReturnAuthUserDetails_whenFound() {
        Page<User> page = new PageImpl<>(List.of(sampleUser));
        when(userRepository.search(any(), any(Pageable.class))).thenReturn(page);

        var result = userService.loadUserByUsername("test@example.com");

        assertThat(result).isInstanceOf(AuthUserDetails.class);
        assertThat(result.getUsername()).isEqualTo("test@example.com");
    }

    @Test
    void loadUserByUsername_shouldThrowUsernameNotFoundException_whenNotFound() {
        when(userRepository.search(any(), any(Pageable.class))).thenReturn(Page.empty());

        assertThatThrownBy(() -> userService.loadUserByUsername("unknown@example.com"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("User not found with email:");
    }

    // ========== registerUser ==========

    @Test
    void registerUser_shouldEncodePasswordAndSaveUser() {
        RequestUser.Register request = new RequestUser.Register(
                "new@example.com", "Password1!", "Jane", "Smith");

        when(passwordEncoder.encode("Password1!")).thenReturn("encodedPwd");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(UUID.randomUUID());
            return u;
        });
        when(jwtService.generateVerifyToken(any(AuthUserDetails.class))).thenReturn("verify-token");

        userService.registerUser(request);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getPassword()).isEqualTo("encodedPwd");
    }

    @Test
    void registerUser_shouldPublishUserCreatedEvent() {
        RequestUser.Register request = new RequestUser.Register(
                "new@example.com", "Password1!", "Jane", "Smith");

        when(passwordEncoder.encode(any())).thenReturn("encodedPwd");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(UUID.randomUUID());
            return u;
        });
        when(jwtService.generateVerifyToken(any(AuthUserDetails.class))).thenReturn("verify-token");

        userService.registerUser(request);

        verify(eventPublisher).publishEvent(any(UserCreatedEvent.class));
    }

    @Test
    void registerUser_shouldGenerateVerifyToken() {
        RequestUser.Register request = new RequestUser.Register(
                "new@example.com", "Password1!", "Jane", "Smith");

        when(passwordEncoder.encode(any())).thenReturn("encodedPwd");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(UUID.randomUUID());
            return u;
        });
        when(jwtService.generateVerifyToken(any(AuthUserDetails.class))).thenReturn("verify-token");

        userService.registerUser(request);

        verify(jwtService).generateVerifyToken(any(AuthUserDetails.class));
    }

    @Test
    void registerUser_shouldMapAllFieldsFromRequest() {
        RequestUser.Register request = new RequestUser.Register(
                "test@test.com", "Password1!", "Alice", "Wonder");

        when(passwordEncoder.encode(any())).thenReturn("enc");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(UUID.randomUUID());
            return u;
        });
        when(jwtService.generateVerifyToken(any())).thenReturn("tok");

        userService.registerUser(request);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User saved = captor.getValue();
        assertThat(saved.getEmail()).isEqualTo("test@test.com");
        assertThat(saved.getFirstName()).isEqualTo("Alice");
        assertThat(saved.getLastName()).isEqualTo("Wonder");
    }

    // ========== getCurrentUser ==========

    @Test
    void getCurrentUser_shouldReturnMeResponse() {
        AuthUserDetails authUser = new AuthUserDetails(sampleUser);
        securityUtilsMock = Mockito.mockStatic(SecurityUtils.class);
        securityUtilsMock.when(SecurityUtils::getCurrentUser).thenReturn(authUser);

        ResponseUser.Me result = userService.getCurrentUser();

        assertThat(result.getId()).isEqualTo(userId);
        assertThat(result.getFirstName()).isEqualTo("John");
        assertThat(result.getLastName()).isEqualTo("Doe");
        assertThat(result.getEmail()).isEqualTo("test@example.com");
    }

    // ========== login ==========

    @Test
    void login_shouldReturnAuthResponse_whenCredentialsValid() {
        RequestUser.Login request = new RequestUser.Login("test@example.com", "password");
        AuthUserDetails authUser = new AuthUserDetails(sampleUser);
        Authentication auth = Mockito.mock(Authentication.class);
        AuthenticationManager authManager = Mockito.mock(AuthenticationManager.class);

        when(authenticationManagerProvider.getObject()).thenReturn(authManager);
        when(authManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(auth);
        when(auth.getPrincipal()).thenReturn(authUser);
        when(jwtService.generateToken(authUser)).thenReturn("jwt-token");

        ResponseUser.AuthResponse result = userService.login(request);

        assertThat(result.getAccessToken()).isEqualTo("jwt-token");
        assertThat(result.getEmail()).isEqualTo("test@example.com");
        assertThat(result.getId()).isEqualTo(userId);
    }

    @Test
    void login_shouldThrowException_whenCredentialsInvalid() {
        RequestUser.Login request = new RequestUser.Login("test@example.com", "wrong");
        AuthenticationManager authManager = Mockito.mock(AuthenticationManager.class);

        when(authenticationManagerProvider.getObject()).thenReturn(authManager);
        when(authManager.authenticate(any())).thenThrow(new BadCredentialsException("Bad credentials"));

        assertThatThrownBy(() -> userService.login(request))
                .isInstanceOf(BadCredentialsException.class);
    }

    // ========== verifyEmail ==========

    @Test
    void verifyEmail_shouldActivateUser_whenTokenValid() {
        RequestUser.VerifyEmail request = new RequestUser.VerifyEmail("valid-token", "test@example.com");
        AuthUserDetails authUser = new AuthUserDetails(sampleUser);

        when(jwtService.isTokenValid("valid-token")).thenReturn(true);
        when(jwtService.getClaimFromToken(any(), any())).thenReturn(true);
        when(jwtService.getCredentialFromToken("valid-token")).thenReturn(authUser);
        when(userRepository.findById(userId)).thenReturn(Optional.of(sampleUser));

        sampleUser.setActive(false);
        userService.verifyEmail(request);

        assertThat(sampleUser.isActive()).isTrue();
        verify(userRepository).save(sampleUser);
    }

    @Test
    void verifyEmail_shouldThrowValidationException_whenTokenInvalid() {
        RequestUser.VerifyEmail request = new RequestUser.VerifyEmail("bad-token", "test@example.com");

        when(jwtService.isTokenValid("bad-token")).thenReturn(false);

        assertThatThrownBy(() -> userService.verifyEmail(request))
                .isInstanceOf(jakarta.validation.ValidationException.class);
    }

    @Test
    void verifyEmail_shouldThrowValidationException_whenMissingVerifyEmailClaim() {
        RequestUser.VerifyEmail request = new RequestUser.VerifyEmail("token", "test@example.com");

        when(jwtService.isTokenValid("token")).thenReturn(true);
        when(jwtService.getClaimFromToken(any(), any())).thenThrow(new RuntimeException("No claim"));

        assertThatThrownBy(() -> userService.verifyEmail(request))
                .isInstanceOf(jakarta.validation.ValidationException.class);
    }

    @Test
    void verifyEmail_shouldThrowValidationException_whenEmailMismatch() {
        RequestUser.VerifyEmail request = new RequestUser.VerifyEmail("token", "other@example.com");

        User otherUser = User.builder().id(UUID.randomUUID()).email("test@example.com")
                .password("p").firstName("A").lastName("B").build();
        AuthUserDetails authUser = new AuthUserDetails(otherUser);

        when(jwtService.isTokenValid("token")).thenReturn(true);
        when(jwtService.getClaimFromToken(any(), any())).thenReturn(true);
        when(jwtService.getCredentialFromToken("token")).thenReturn(authUser);

        assertThatThrownBy(() -> userService.verifyEmail(request))
                .isInstanceOf(jakarta.validation.ValidationException.class)
                .hasMessageContaining("Token does not match");
    }

    @Test
    void verifyEmail_shouldThrowValidationException_whenUserNotFound() {
        RequestUser.VerifyEmail request = new RequestUser.VerifyEmail("token", "test@example.com");
        AuthUserDetails authUser = new AuthUserDetails(sampleUser);

        when(jwtService.isTokenValid("token")).thenReturn(true);
        when(jwtService.getClaimFromToken(any(), any())).thenReturn(true);
        when(jwtService.getCredentialFromToken("token")).thenReturn(authUser);
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.verifyEmail(request))
                .isInstanceOf(jakarta.validation.ValidationException.class)
                .hasMessageContaining("User not found");
    }
}
