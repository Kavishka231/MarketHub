package com.markethub.auth;

import com.markethub.user.User;
import com.markethub.user.UserRole;
import com.markethub.user.UserStatus;

public record AuthUserResponse(
        Long id,
        String firstName,
        String lastName,
        String email,
        UserRole role,
        UserStatus status) {

    static AuthUserResponse from(User user) {
        return new AuthUserResponse(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getRole(),
                user.getStatus());
    }
}
