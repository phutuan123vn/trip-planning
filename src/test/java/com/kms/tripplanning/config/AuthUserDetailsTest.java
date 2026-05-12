package com.kms.tripplanning.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.OffsetDateTime;
import java.util.Date;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import com.kms.tripplanning.entity.User;

class AuthUserDetailsTest {

    private User activeUser;
    private User lockedUser;
    private User inactiveUser;
    private User deletedUser;

    @BeforeEach
    void setUp() {
        activeUser = User.builder()
                .id(UUID.randomUUID())
                .email("test@example.com")
                .password("encoded_pass")
                .firstName("John")
                .lastName("Doe")
                .isActive(true)
                .isLocked(false)
                .build();
        activeUser.setDeletedAt(null);

        lockedUser = User.builder()
                .isActive(true)
                .isLocked(true)
                .build();

        inactiveUser = User.builder()
                .isActive(false)
                .isLocked(false)
                .build();

        deletedUser = User.builder()
                .isActive(true)
                .isLocked(false)
                .build();
        deletedUser.setDeletedAt(OffsetDateTime.now());
    }

    @Test
    void getAuthorities_shouldReturnRoleUser() {
        AuthUserDetails details = new AuthUserDetails(activeUser);
        assertThat(details.getAuthorities())
                .hasSize(1)
                .first()
                .extracting("authority")
                .isEqualTo("ROLE_USER");
    }

    @Test
    void getPassword_shouldReturnUserPassword() {
        AuthUserDetails details = new AuthUserDetails(activeUser);
        assertThat(details.getPassword()).isEqualTo("encoded_pass");
    }

    @Test
    void getUsername_shouldReturnUserEmail() {
        AuthUserDetails details = new AuthUserDetails(activeUser);
        assertThat(details.getUsername()).isEqualTo("test@example.com");
    }

    @Test
    void getFirstName_shouldReturnUserFirstName() {
        AuthUserDetails details = new AuthUserDetails(activeUser);
        assertThat(details.getFirstName()).isEqualTo("John");
    }

    @Test
    void getLastName_shouldReturnUserLastName() {
        AuthUserDetails details = new AuthUserDetails(activeUser);
        assertThat(details.getLastName()).isEqualTo("Doe");
    }

    @Test
    void getId_shouldReturnUserId() {
        AuthUserDetails details = new AuthUserDetails(activeUser);
        assertThat(details.getId()).isEqualTo(activeUser.getId());
    }

    @Test
    void isEnabled_shouldReturnTrue_whenActiveAndNotDeletedAndNotLocked() {
        AuthUserDetails details = new AuthUserDetails(activeUser);
        assertThat(details.isEnabled()).isTrue();
    }

    @Test
    void isEnabled_shouldReturnFalse_whenNotActive() {
        AuthUserDetails details = new AuthUserDetails(inactiveUser);
        assertThat(details.isEnabled()).isFalse();
    }

    @Test
    void isEnabled_shouldReturnFalse_whenLocked() {
        AuthUserDetails details = new AuthUserDetails(lockedUser);
        assertThat(details.isEnabled()).isFalse();
    }

    @Test
    void isEnabled_shouldReturnFalse_whenDeleted() {
        AuthUserDetails details = new AuthUserDetails(deletedUser);
        assertThat(details.isEnabled()).isFalse();
    }

    @Test
    void isAccountNonLocked_shouldReturnTrue_whenNotLockedAndNotDeleted() {
        AuthUserDetails details = new AuthUserDetails(activeUser);
        assertThat(details.isAccountNonLocked()).isTrue();
    }

    @Test
    void isAccountNonLocked_shouldReturnFalse_whenLocked() {
        AuthUserDetails details = new AuthUserDetails(lockedUser);
        assertThat(details.isAccountNonLocked()).isFalse();
    }
}
