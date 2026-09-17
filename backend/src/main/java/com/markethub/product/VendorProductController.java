package com.markethub.product;

import com.markethub.vendor.VendorAccessDeniedException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/vendor/products")
public class VendorProductController {
    private final ProductService service;
    public VendorProductController(ProductService service) { this.service = service; }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductResponse create(Authentication authentication, @Valid @RequestBody ProductRequest request) {
        requireVendor(authentication);
        return service.create(authentication.getName(), request);
    }

    static void requireVendor(Authentication authentication) {
        boolean vendor = authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_VENDOR"));
        if (!vendor) throw new VendorAccessDeniedException("Vendor role is required");
    }
}
