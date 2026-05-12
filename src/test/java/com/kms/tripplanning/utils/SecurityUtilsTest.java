package com.kms.tripplanning.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import com.kms.tripplanning.config.AuthUserDetails;

class SecurityUtilsTest {

    private SecurityContext securityContext;
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        securityContext = mock(SecurityContext.class);
        authentication = mock(Authentication.class);
        SecurityContextHolder.setContext(securityContext);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getCurrentUser_shouldReturnAuthUserDetails_whenAuthenticated() {
        AuthUserDetails userDetails = mock(AuthUserDetails.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(userDetails);

        AuthUserDetails result = SecurityUtils.getCurrentUser();

        assertThat(result).isSameAs(userDetails);
    }

    @Test
    void getCurrentUser_shouldReturnNull_whenNoAuthentication() {
        when(securityContext.getAuthentication()).thenReturn(null);

        AuthUserDetails result = SecurityUtils.getCurrentUser();

        assertThat(result).isNull();
    }

    @Test
    void getCurrentUser_shouldReturnNull_whenNotAuthenticated() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(false);

        AuthUserDetails result = SecurityUtils.getCurrentUser();

        assertThat(result).isNull();
    }

    @Test
    void getCurrentUser_shouldReturnNull_whenPrincipalNotAuthUserDetails() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn("NotAuthUserDetails");

        AuthUserDetails result = SecurityUtils.getCurrentUser();

        assertThat(result).isNull();
    }
}
