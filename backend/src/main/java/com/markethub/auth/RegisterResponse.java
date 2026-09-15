package com.markethub.auth;

import com.markethub.user.UserRole;
import com.markethub.user.UserStatus;

import java.time.Instant;

public record RegisterResponse(
        Long id,
        String firstName,
        String lastName,
        String email,
        String phone,
        UserRole role,
        UserStatus status,
        Instant createdAt) {
}
