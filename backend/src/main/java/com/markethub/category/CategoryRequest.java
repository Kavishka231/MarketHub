package com.markethub.category;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoryRequest(
        @NotBlank(message = "Category name is required")
        @Size(max = 120, message = "Category name must contain at most 120 characters")
        String name,

        @Size(max = 1000, message = "Description must contain at most 1000 characters")
        String description) {
}
