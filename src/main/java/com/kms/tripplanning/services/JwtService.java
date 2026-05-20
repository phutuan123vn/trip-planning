package com.kms.tripplanning.services;

import java.util.function.Function;

import com.kms.tripplanning.config.AuthUserDetails;

import io.jsonwebtoken.Claims;

public interface JwtService {

    String generateToken(AuthUserDetails userDetails);

    String generateVerifyToken(AuthUserDetails userDetails);

    boolean isTokenValid(String token);

    AuthUserDetails getCredentialFromToken(String token);

    <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver);
}
