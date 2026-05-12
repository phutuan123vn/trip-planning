package com.kms.tripplanning.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import com.kms.tripplanning.config.AuthUserDetails;

class AuditMixinListenerTest {

    private AuditMixinListener listener;
    private SecurityContext securityContext;
    private Authentication authentication;
    private AuthUserDetails userDetails;
    private UUID userId;

    private static class TestEntity extends AuditMixin {}

    @BeforeEach
    void setUp() {
        listener = new AuditMixinListener();
        securityContext = mock(SecurityContext.class);
        authentication = mock(Authentication.class);
        userDetails = mock(AuthUserDetails.class);
        userId = UUID.randomUUID();

        SecurityContextHolder.setContext(securityContext);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void mockAuthentication() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getId()).thenReturn(userId);
    }

    @Test
    void logUserBefore_shouldSetCreatedBy_onNewEntity() {
        mockAuthentication();
        TestEntity entity = new TestEntity();

        listener.logUserBefore(entity);

        assertThat(entity.getCreatedBy()).isEqualTo(userId.toString());
        assertThat(entity.getUpdatedBy()).isNull();
        assertThat(entity.getDeletedBy()).isNull();
    }

    @Test
    void logUserBefore_shouldSetUpdatedBy_onExistingEntity() {
        mockAuthentication();
        TestEntity entity = new TestEntity();
        entity.setCreatedBy("another-user");

        listener.logUserBefore(entity);

        assertThat(entity.getCreatedBy()).isEqualTo("another-user");
        assertThat(entity.getUpdatedBy()).isEqualTo(userId.toString());
        assertThat(entity.getDeletedBy()).isNull();
    }

    @Test
    void logUserBefore_shouldSetDeletedBy_onDeletedEntity() {
        mockAuthentication();
        TestEntity entity = new TestEntity();
        entity.setCreatedBy("another-user");
        entity.setDeletedAt(OffsetDateTime.now());

        listener.logUserBefore(entity);

        assertThat(entity.getDeletedBy()).isEqualTo(userId.toString());
    }

    @Test
    void logUserBefore_shouldSkip_whenNotAuditMixin() {
        mockAuthentication();
        Object notEntity = new Object();

        listener.logUserBefore(notEntity);
        // Should not throw any exception
    }

    @Test
    void logUserBefore_shouldSkip_whenNoCurrentUser() {
        when(securityContext.getAuthentication()).thenReturn(null);
        TestEntity entity = new TestEntity();

        listener.logUserBefore(entity);

        assertThat(entity.getCreatedBy()).isNull();
        assertThat(entity.getUpdatedBy()).isNull();
    }
}
