package com.markethub.auth;

public record LoginResponse(
        String token,
        String tokenType,
        long expiresIn,
        AuthUserResponse user) {
}
