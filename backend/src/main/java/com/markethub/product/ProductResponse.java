package com.markethub.product;

import java.math.BigDecimal;
import java.time.Instant;

public record ProductResponse(Long id, String name, String description, BigDecimal price,
        int stockQuantity, String imageUrl, ProductStatus status, Long categoryId,
        String categoryName, Long vendorId, String vendorName, Instant createdAt, Instant updatedAt) {
    public static ProductResponse from(Product product) {
        return new ProductResponse(product.getId(), product.getName(), product.getDescription(), product.getPrice(),
                product.getStockQuantity(), product.getImageUrl(), product.getStatus(), product.getCategory().getId(),
                product.getCategory().getName(), product.getVendor().getId(), product.getVendor().getStoreName(),
                product.getCreatedAt(), product.getUpdatedAt());
    }
}
