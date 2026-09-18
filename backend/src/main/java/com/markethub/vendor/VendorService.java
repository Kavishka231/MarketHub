package com.markethub.vendor;

import com.markethub.user.User;
import com.markethub.user.UserRepository;
import com.markethub.user.UserRole;
import com.markethub.user.UserStatus;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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

        Vendor vendor = new Vendor(
                user,
                request.storeName(),
                request.description(),
                request.phone());
        vendor.setStatus(VendorStatus.PENDING);

        try {
            return VendorResponse.from(vendorRepository.saveAndFlush(vendor));
        } catch (DataIntegrityViolationException exception) {
            throw new DuplicateVendorApplicationException();
        }
    }

    @Transactional(readOnly = true)
    public VendorResponse getCurrentVendor(String email) {
        User user = findAuthenticatedUser(email);
        Vendor vendor = vendorRepository.findByUserId(user.getId())
                .orElseThrow(VendorNotFoundException::new);
        return VendorResponse.from(vendor);
    }

    @Transactional(readOnly = true)
    public List<VendorResponse> list(VendorStatus status) {
        List<Vendor> vendors = status == null
                ? vendorRepository.findAll()
                : vendorRepository.findByStatus(status);
        return vendors.stream().map(VendorResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public VendorResponse get(Long vendorId) {
        return VendorResponse.from(vendorRepository.findById(vendorId).orElseThrow(VendorNotFoundException::new));
    }

    @Transactional
    public VendorResponse suspend(Long vendorId) {
        Vendor vendor = vendorRepository.findById(vendorId).orElseThrow(VendorNotFoundException::new);
        if (vendor.getStatus() != VendorStatus.APPROVED) throw new InvalidVendorStatusException(vendor.getStatus());
        vendor.setStatus(VendorStatus.SUSPENDED);
        return VendorResponse.from(vendorRepository.saveAndFlush(vendor));
    }

    @Transactional
    public VendorResponse approve(Long vendorId) {
        Vendor vendor = findPendingVendor(vendorId);
        vendor.setStatus(VendorStatus.APPROVED);
        vendor.getUser().setRole(UserRole.VENDOR);
        userRepository.save(vendor.getUser());
        return VendorResponse.from(vendorRepository.saveAndFlush(vendor));
    }

    @Transactional
    public VendorResponse reject(Long vendorId) {
        Vendor vendor = findPendingVendor(vendorId);
        vendor.setStatus(VendorStatus.REJECTED);
        return VendorResponse.from(vendorRepository.saveAndFlush(vendor));
    }

    private User findAuthenticatedUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new VendorAccessDeniedException("Authenticated user was not found"));
    }

    private Vendor findPendingVendor(Long vendorId) {
        Vendor vendor = vendorRepository.findById(vendorId)
                .orElseThrow(VendorNotFoundException::new);
        if (vendor.getStatus() != VendorStatus.PENDING) {
            throw new InvalidVendorStatusException(vendor.getStatus());
        }
        return vendor;
    }
}
