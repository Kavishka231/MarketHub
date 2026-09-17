package com.markethub.address;

import com.markethub.user.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class AddressService {
    private final AddressRepository addresses;
    private final UserRepository users;

    public AddressService(AddressRepository addresses, UserRepository users) {
        this.addresses = addresses; this.users = users;
    }

    @Transactional
    public AddressResponse create(String email, AddressRequest request) {
        User customer = customer(email);
        boolean makeDefault = addresses.countByUserId(customer.getId()) == 0
                || Boolean.TRUE.equals(request.defaultAddress());
        if (makeDefault) clearDefault(customer.getId());
        Address address = new Address(customer, normalize(request.fullName()), normalize(request.phone()),
                normalize(request.addressLine1()), optional(request.addressLine2()), normalize(request.city()),
                normalize(request.district()), optional(request.postalCode()), makeDefault);
        return AddressResponse.from(addresses.saveAndFlush(address));
    }

    @Transactional(readOnly = true)
    public List<AddressResponse> list(String email) {
        User customer = customer(email);
        return addresses.findByUserIdOrderByDefaultAddressDescCreatedAtDesc(customer.getId())
                .stream().map(AddressResponse::from).toList();
    }

    User customer(String email) {
        User user = users.findByEmail(email)
                .orElseThrow(() -> new AddressAccessDeniedException("Customer account is required"));
        if (user.getRole() != UserRole.CUSTOMER) throw new AddressAccessDeniedException("Customer role is required");
        return user;
    }

    void clearDefault(Long userId) {
        List<Address> existing = addresses.findByUserIdOrderByCreatedAtAsc(userId);
        existing.forEach(address -> address.setDefaultAddress(false));
        addresses.saveAll(existing);
    }

    static String normalize(String value) { return value.trim().replaceAll("\\s+", " "); }
    static String optional(String value) { return value == null || value.isBlank() ? null : normalize(value); }
}
