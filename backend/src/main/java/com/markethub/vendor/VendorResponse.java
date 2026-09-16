package com.markethub.vendor;

import java.time.Instant;

public record VendorResponse(
        Long id,
        Long userId,
        String storeName,
        String description,
        String phone,
        VendorStatus status,
        Instant createdAt,
        Instant updatedAt) {

    static VendorResponse from(Vendor vendor) {
        return new VendorResponse(
                vendor.getId(),
                vendor.getUser().getId(),
                vendor.getStoreName(),
                vendor.getDescription(),
                vendor.getPhone(),
                vendor.getStatus(),
                vendor.getCreatedAt(),
                vendor.getUpdatedAt());
    }
}
