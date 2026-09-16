package com.markethub.vendor;

import com.markethub.user.User;
import com.markethub.user.UserRepository;
import com.markethub.user.UserRole;
import com.markethub.user.UserStatus;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VendorService {

    private final VendorRepository vendorRepository;
    private final UserRepository userRepository;

    public VendorService(VendorRepository vendorRepository, UserRepository userRepository) {
        this.vendorRepository = vendorRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public VendorResponse apply(String email, VendorApplicationRequest request) {
        User user = findAuthenticatedUser(email);
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new VendorAccessDeniedException("Active account is required");
        }
        if (user.getRole() != UserRole.CUSTOMER) {
            throw new VendorAccessDeniedException("Only customers can apply to become vendors");
        }
        if (vendorRepository.existsByUserId(user.getId())) {
            throw new DuplicateVendorApplicationException();
        }

        Vendor vendor = new Vendor(user, request.storeName(), request.description(), request.phone());
        vendor.setStatus(VendorStatus.PENDING);

        try {
            return VendorResponse.from(vendorRepository.saveAndFlush(vendor));
        } catch (DataIntegrityViolationException exception) {
            throw new DuplicateVendorApplicationException();
        }
    }

    private User findAuthenticatedUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new VendorAccessDeniedException("Authenticated user was not found"));
    }
}
