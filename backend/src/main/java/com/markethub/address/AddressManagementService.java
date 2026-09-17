package com.markethub.address;

import com.markethub.user.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class AddressManagementService {
    private final AddressRepository addresses;
    private final UserRepository users;

    public AddressManagementService(AddressRepository addresses, UserRepository users) {
        this.addresses = addresses; this.users = users;
    }

    @Transactional
    public AddressResponse update(String email, Long id, AddressUpdateRequest request) {
        Address address = owned(email, id);
        address.setFullName(AddressService.normalize(request.fullName()));
        address.setPhone(AddressService.normalize(request.phone()));
        address.setAddressLine1(AddressService.normalize(request.addressLine1()));
        address.setAddressLine2(AddressService.optional(request.addressLine2()));
        address.setCity(AddressService.normalize(request.city()));
        address.setDistrict(AddressService.normalize(request.district()));
        address.setPostalCode(AddressService.optional(request.postalCode()));
        return AddressResponse.from(addresses.saveAndFlush(address));
    }

    @Transactional
    public void delete(String email, Long id) {
        Address address = owned(email, id);
        List<Address> remaining = addresses.findByUserIdOrderByCreatedAtAsc(address.getUser().getId())
                .stream().filter(candidate -> !candidate.getId().equals(id)).toList();
        addresses.delete(address);
        if (address.isDefaultAddress() && !remaining.isEmpty()) {
            Address oldest = remaining.get(0);
            oldest.setDefaultAddress(true);
            addresses.save(oldest);
        }
    }

    @Transactional
    public AddressResponse makeDefault(String email, Long id) {
        Address selected = owned(email, id);
        List<Address> customerAddresses = addresses.findByUserIdOrderByCreatedAtAsc(selected.getUser().getId());
        customerAddresses.forEach(address -> address.setDefaultAddress(address.getId().equals(selected.getId())));
        addresses.saveAll(customerAddresses);
        addresses.flush();
        return AddressResponse.from(selected);
    }

    private Address owned(String email, Long id) {
        User customer = customer(email);
        Address address = addresses.findById(id).orElseThrow(AddressNotFoundException::new);
        if (!address.getUser().getId().equals(customer.getId())) throw new AddressOwnershipException();
        return address;
    }

    private User customer(String email) {
        User user = users.findByEmail(email)
                .orElseThrow(() -> new AddressAccessDeniedException("Customer account is required"));
        if (user.getRole() != UserRole.CUSTOMER) throw new AddressAccessDeniedException("Customer role is required");
        return user;
    }
}
