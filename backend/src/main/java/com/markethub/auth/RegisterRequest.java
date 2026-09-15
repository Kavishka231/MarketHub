package com.markethub.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "First name is required")
        @Size(max = 100, message = "First name must contain at most 100 characters")
        String firstName,

        @NotBlank(message = "Last name is required")
        @Size(max = 100, message = "Last name must contain at most 100 characters")
        String lastName,

        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        @Size(max = 255, message = "Email must contain at most 255 characters")
        String email,

        @NotBlank(message = "Password is required")
        @Size(min = 8, max = 72, message = "Password must contain between 8 and 72 characters")
        String password,

        @Size(max = 30, message = "Phone must contain at most 30 characters")
        @Pattern(
                regexp = "^\\+?[0-9][0-9 ()-]{6,19}$",
                message = "Phone number must be valid")
        String phone) {

    public RegisterRequest {
        firstName = trim(firstName);
        lastName = trim(lastName);
        email = trim(email);
        phone = trimToNull(phone);
    }

    private static String trim(String value) {
        return value == null ? null : value.trim();
    }

    private static String trimToNull(String value) {
        String trimmedValue = trim(value);
        return trimmedValue == null || trimmedValue.isEmpty() ? null : trimmedValue;
    }
}
