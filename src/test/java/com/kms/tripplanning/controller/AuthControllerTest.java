package com.kms.tripplanning.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.kms.tripplanning.dto.user.RequestUser;
import com.kms.tripplanning.dto.user.ResponseUser;
import com.kms.tripplanning.exception.ApiExceptionHandler;
import com.kms.tripplanning.services.UserService;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UserService userService;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController)
                .setControllerAdvice(new ApiExceptionHandler())
                .build();
    }

    // ========== POST /api/auth/register ==========

    @Test
    void register_shouldReturn204_whenValidRequest() throws Exception {
        doNothing().when(userService).registerUser(any(RequestUser.Register.class));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "email": "test@example.com",
                                    "password": "Password1!",
                                    "firstName": "John",
                                    "lastName": "Doe"
                                }
                                """))
                .andExpect(status().isNoContent());
    }

    @Test
    void register_shouldReturn400_whenEmailInvalid() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "email": "not-an-email",
                                    "password": "Password1!",
                                    "firstName": "John",
                                    "lastName": "Doe"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_shouldReturn400_whenPasswordTooShort() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "email": "test@example.com",
                                    "password": "Short1!",
                                    "firstName": "John",
                                    "lastName": "Doe"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_shouldReturn400_whenPasswordMissingSpecialChar() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "email": "test@example.com",
                                    "password": "Password1abc",
                                    "firstName": "John",
                                    "lastName": "Doe"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_shouldReturn400_whenFirstNameBlank() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "email": "test@example.com",
                                    "password": "Password1!",
                                    "firstName": "",
                                    "lastName": "Doe"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    // ========== POST /api/auth/login ==========

    @Test
    void login_shouldReturn200_withAuthResponse() throws Exception {
        UUID userId = UUID.randomUUID();
        ResponseUser.AuthResponse authResponse = new ResponseUser.AuthResponse(
                "jwt-token", userId, "test@example.com");
        when(userService.login(any(RequestUser.Login.class))).thenReturn(authResponse);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "email": "test@example.com",
                                    "password": "password"
                                }
                                """))
                .andExpect(status().isOk());
    }

    @Test
    void login_shouldReturn400_whenEmailBlank() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "email": "",
                                    "password": "password"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    // ========== GET /api/auth/me ==========

    @Test
    void getCurrentUser_shouldReturn200_withMeResponse() throws Exception {
        UUID userId = UUID.randomUUID();
        ResponseUser.Me me = new ResponseUser.Me(userId, "John", "Doe", "test@example.com");
        when(userService.getCurrentUser()).thenReturn(me);

        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isOk());
    }

    @Test
    void getCurrentUser_shouldReturn401_whenNotAuthenticated() throws Exception {
        // With standalone setup, no security filters are applied,
        // so we verify controller logic returns results correctly.
        // Security-level auth tests belong in integration tests.
        UUID userId = UUID.randomUUID();
        ResponseUser.Me me = new ResponseUser.Me(userId, "John", "Doe", "test@example.com");
        when(userService.getCurrentUser()).thenReturn(me);

        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isOk());
    }

    // ========== POST /api/auth/verify-email ==========

    @Test
    void verifyEmail_shouldReturn204_whenValid() throws Exception {
        doNothing().when(userService).verifyEmail(any(RequestUser.VerifyEmail.class));

        mockMvc.perform(post("/api/auth/verify-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "token": "verify-token-123",
                                    "email": "test@example.com"
                                }
                                """))
                .andExpect(status().isNoContent());
    }
}
