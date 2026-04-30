package com.kms.tripplanning.services.Impl;

import java.util.Base64;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;

import com.kms.tripplanning.config.AuthUserDetails;
import com.kms.tripplanning.services.JwtService;
import com.kms.tripplanning.services.UserService;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class JwtServiceImpl implements JwtService {

    @Value("${spring.jwt.secret-key}")
    private String secretKey;

    @Value("${spring.jwt.expiration}")
    private long expiration;

    private final ObjectProvider<UserService> userServiceProvider;

    @Override
    public String generateToken(AuthUserDetails userDetails) {
        String token = Jwts.builder()
                .id(userDetails.getId())
                .subject(userDetails.getUsername())
                .claims(Map.ofEntries(
                        Map.entry("firstName", userDetails.getFirstName()),
                        Map.entry("lastName", userDetails.getLastName())))
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .issuedAt(new Date())
                .signWith(getSigningKey())
                .compact();
        return token;
    }

    @Override
    public boolean isTokenValid(String token) {
        var claims = getClaimsFromToken(token);
        return !claims.getExpiration().before(new Date());
    }

    @Override
    public AuthUserDetails getCredentialFromToken(String token) {
        String email = getClaimFromToken(token, Claims::getSubject);
        return (AuthUserDetails) userServiceProvider.getObject().loadUserByUsername(email);
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = Base64.getDecoder().decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    @Override
    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        Claims claims = getClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    private Claims getClaimsFromToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            throw new BadCredentialsException("Invalid JWT token: " + e.getMessage());
        }
    }

    @Override
    public String generateVerifyToken(AuthUserDetails userDetails) {
        String token = Jwts.builder()
                .id(userDetails.getId())
                .subject(userDetails.getUsername())
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 5)) // 5 minutes
                .claims(Map.ofEntries(
                        Map.entry("isVerifyEmail", true)))
                .issuedAt(new Date())
                .signWith(getSigningKey())
                .compact();
        return token;
    }

}
