package com.markethub.product;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record ProductRequest(
        @NotBlank(message = "Product name is required") @Size(max = 200) String name,
        @Size(max = 4000) String description,
        @NotNull(message = "Category is required") Long categoryId,
        @NotNull(message = "Price is required") @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than zero") BigDecimal price,
        @NotNull(message = "Stock quantity is required") @Min(value = 0, message = "Stock quantity cannot be negative") Integer stockQuantity,
        @Size(max = 1000) String imageUrl) {}
