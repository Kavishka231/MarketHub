package com.markethub.vendor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record VendorApplicationRequest(
        @NotBlank(message = "Store name is required")
        @Size(max = 150, message = "Store name must contain at most 150 characters")
        String storeName,

        @Size(max = 2000, message = "Description must contain at most 2000 characters")
        String description,

        @Size(max = 30, message = "Phone must contain at most 30 characters")
        @Pattern(
                regexp = "^\\+?[0-9][0-9 ()-]{6,19}$",
                message = "Phone number must be valid")
        String phone) {

    public VendorApplicationRequest {
        storeName = trim(storeName);
        description = trimToNull(description);
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
