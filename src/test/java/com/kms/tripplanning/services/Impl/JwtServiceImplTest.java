package com.kms.tripplanning.services.Impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.Base64;
import java.util.UUID;

import javax.crypto.SecretKey;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.util.ReflectionTestUtils;

import com.kms.tripplanning.config.AuthUserDetails;
import com.kms.tripplanning.entity.User;
import com.kms.tripplanning.services.UserService;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@ExtendWith(MockitoExtension.class)
class JwtServiceImplTest {

    @Mock
    private ObjectProvider<UserService> userServiceProvider;

    @Mock
    private UserService userService;

    private JwtServiceImpl jwtService;
    private AuthUserDetails authUserDetails;
    private UUID userId;
    private String testSecretKey;

    @BeforeEach
    void setUp() {
        jwtService = new JwtServiceImpl(userServiceProvider);

        // Generate a valid Base64-encoded 256-bit key
        SecretKey key = Keys.secretKeyFor(io.jsonwebtoken.SignatureAlgorithm.HS256);
        testSecretKey = Base64.getEncoder().encodeToString(key.getEncoded());

        ReflectionTestUtils.setField(jwtService, "secretKey", testSecretKey);
        ReflectionTestUtils.setField(jwtService, "expiration", 3600000L); // 1 hour

        userId = UUID.randomUUID();
        User user = User.builder()
                .id(userId)
                .email("test@example.com")
                .password("password")
                .firstName("John")
                .lastName("Doe")
                .isActive(true)
                .build();
        authUserDetails = new AuthUserDetails(user);
    }

    // ========== generateToken ==========

    @Test
    void generateToken_shouldReturnNonNullToken() {
        String token = jwtService.generateToken(authUserDetails);

        assertThat(token).isNotNull().isNotEmpty();
    }

    @Test
    void generateToken_shouldContainSubject() {
        String token = jwtService.generateToken(authUserDetails);

        String subject = jwtService.getClaimFromToken(token, Claims::getSubject);
        assertThat(subject).isEqualTo("test@example.com");
    }

    @Test
    void generateToken_shouldContainFirstAndLastNameClaims() {
        String token = jwtService.generateToken(authUserDetails);

        String firstName = jwtService.getClaimFromToken(token, claims -> claims.get("firstName", String.class));
        String lastName = jwtService.getClaimFromToken(token, claims -> claims.get("lastName", String.class));

        assertThat(firstName).isEqualTo("John");
        assertThat(lastName).isEqualTo("Doe");
    }

    @Test
    void generateToken_shouldContainIdClaim() {
        String token = jwtService.generateToken(authUserDetails);

        String jti = jwtService.getClaimFromToken(token, Claims::getId);
        assertThat(jti).isEqualTo(userId.toString());
    }

    @Test
    void generateToken_shouldHaveCorrectExpiration() {
        long before = System.currentTimeMillis();
        String token = jwtService.generateToken(authUserDetails);
        long after = System.currentTimeMillis();

        long expiration = jwtService.getClaimFromToken(token, Claims::getExpiration).getTime();

        // Expiration should be approximately now + 1 hour (within a 5-second tolerance)
        assertThat(expiration).isBetween(before + 3600000L - 5000, after + 3600000L + 5000);
    }

    // ========== generateVerifyToken ==========

    @Test
    void generateVerifyToken_shouldContainIsVerifyEmailClaim() {
        String token = jwtService.generateVerifyToken(authUserDetails);

        Boolean isVerify = jwtService.getClaimFromToken(token,
                claims -> claims.get("isVerifyEmail", Boolean.class));
        assertThat(isVerify).isTrue();
    }

    @Test
    void generateVerifyToken_shouldExpireIn5Minutes() {
        long before = System.currentTimeMillis();
        String token = jwtService.generateVerifyToken(authUserDetails);
        long after = System.currentTimeMillis();

        long expiration = jwtService.getClaimFromToken(token, Claims::getExpiration).getTime();
        long fiveMinMs = 1000 * 60 * 5;

        assertThat(expiration).isBetween(before + fiveMinMs - 5000, after + fiveMinMs + 5000);
    }

    // ========== isTokenValid ==========

    @Test
    void isTokenValid_shouldReturnTrue_forValidToken() {
        String token = jwtService.generateToken(authUserDetails);

        assertThat(jwtService.isTokenValid(token)).isTrue();
    }

    @Test
    void isTokenValid_shouldThrowBadCredentials_forExpiredToken() {
        // Set expiration to -1 ms (already expired)
        ReflectionTestUtils.setField(jwtService, "expiration", -1000L);
        String token = jwtService.generateToken(authUserDetails);

        // Restore for parsing - the token itself is already expired
        ReflectionTestUtils.setField(jwtService, "expiration", 3600000L);

        assertThatThrownBy(() -> jwtService.isTokenValid(token))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void isTokenValid_shouldThrowBadCredentials_forTamperedToken() {
        String token = jwtService.generateToken(authUserDetails);
        String tampered = token + "tampered";

        assertThatThrownBy(() -> jwtService.isTokenValid(tampered))
                .isInstanceOf(BadCredentialsException.class);
    }

    // ========== getCredentialFromToken ==========

    @Test
    void getCredentialFromToken_shouldReturnAuthUserDetails() {
        String token = jwtService.generateToken(authUserDetails);

        when(userServiceProvider.getObject()).thenReturn(userService);
        when(userService.loadUserByUsername("test@example.com")).thenReturn(authUserDetails);

        AuthUserDetails result = jwtService.getCredentialFromToken(token);

        assertThat(result.getUsername()).isEqualTo("test@example.com");
    }

    // ========== getClaimFromToken ==========

    @Test
    void getClaimFromToken_shouldExtractSubject() {
        String token = jwtService.generateToken(authUserDetails);

        String subject = jwtService.getClaimFromToken(token, Claims::getSubject);

        assertThat(subject).isEqualTo("test@example.com");
    }

    @Test
    void getClaimFromToken_shouldExtractCustomClaim() {
        String token = jwtService.generateToken(authUserDetails);

        String firstName = jwtService.getClaimFromToken(token,
                claims -> claims.get("firstName", String.class));

        assertThat(firstName).isEqualTo("John");
    }
}
