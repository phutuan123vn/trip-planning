package com.kms.tripplanning.filter;

import static org.mockito.Mockito.*;

import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import com.kms.tripplanning.config.AuthUserDetails;
import com.kms.tripplanning.entity.User;
import com.kms.tripplanning.services.JwtService;

import jakarta.servlet.FilterChain;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class JwtFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtFilter jwtFilter;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilterInternal_shouldContinueChain_whenNoAuthHeader() throws Exception {
        // No Authorization header set
        jwtFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(jwtService, never()).isTokenValid(any());
    }

    @Test
    void doFilterInternal_shouldContinueChain_whenAuthHeaderNotBearer() throws Exception {
        request.addHeader("Authorization", "Basic sometoken");

        jwtFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(jwtService, never()).isTokenValid(any());
    }

    @Test
    void doFilterInternal_shouldContinueChain_whenTokenInvalid() throws Exception {
        request.addHeader("Authorization", "Bearer invalid-token");
        when(jwtService.isTokenValid("invalid-token")).thenReturn(false);

        jwtFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(jwtService, never()).getCredentialFromToken(any());
    }

    @Test
    void doFilterInternal_shouldSetAuthentication_whenTokenValid() throws Exception {
        String token = "valid-jwt-token";
        request.addHeader("Authorization", "Bearer " + token);

        User user = User.builder()
                .id(UUID.randomUUID())
                .email("test@test.com")
                .password("pass")
                .firstName("John")
                .lastName("Doe")
                .isActive(true)
                .build();
        AuthUserDetails authUser = new AuthUserDetails(user);

        when(jwtService.isTokenValid(token)).thenReturn(true);
        when(jwtService.getCredentialFromToken(token)).thenReturn(authUser);

        jwtFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).isEqualTo(authUser);
    }

    @Test
    void doFilterInternal_shouldSkipAuth_whenAlreadyAuthenticated() throws Exception {
        String token = "valid-jwt-token";
        request.addHeader("Authorization", "Bearer " + token);

        // Pre-set authentication
        User user = User.builder()
                .id(UUID.randomUUID())
                .email("existing@test.com")
                .password("pass")
                .firstName("Jane")
                .lastName("Doe")
                .isActive(true)
                .build();
        AuthUserDetails existingUser = new AuthUserDetails(user);
        var existingAuth = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                existingUser, null, existingUser.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(existingAuth);

        when(jwtService.isTokenValid(token)).thenReturn(true);

        jwtFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(jwtService, never()).getCredentialFromToken(any());
    }

    @Test
    void doFilterInternal_shouldExtractTokenFromHeader() throws Exception {
        request.addHeader("Authorization", "Bearer my-special-token");
        when(jwtService.isTokenValid("my-special-token")).thenReturn(false);

        jwtFilter.doFilterInternal(request, response, filterChain);

        // Verify the "Bearer " prefix was stripped correctly
        verify(jwtService).isTokenValid("my-special-token");
    }
}
