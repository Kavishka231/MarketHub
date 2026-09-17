package com.markethub.address;

import java.time.Instant;

public record AddressResponse(Long id, String fullName, String phone, String addressLine1,
        String addressLine2, String city, String district, String postalCode,
        boolean defaultAddress, Instant createdAt, Instant updatedAt) {
    public static AddressResponse from(Address address) {
        return new AddressResponse(address.getId(), address.getFullName(), address.getPhone(),
                address.getAddressLine1(), address.getAddressLine2(), address.getCity(),
                address.getDistrict(), address.getPostalCode(), address.isDefaultAddress(),
                address.getCreatedAt(), address.getUpdatedAt());
    }
}
