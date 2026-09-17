package com.markethub.product;

import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/vendor/products")
public class VendorProductManagementController {
    private final VendorProductManagementService service;
    public VendorProductManagementController(VendorProductManagementService service) { this.service = service; }

    @GetMapping
    public ProductPageResponse list(Authentication auth, @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        VendorProductController.requireVendor(auth);
        return service.list(auth.getName(), page, Math.min(size, 100));
    }

    @GetMapping("/{productId}")
    public ProductResponse get(Authentication auth, @PathVariable Long productId) {
        VendorProductController.requireVendor(auth); return service.get(auth.getName(), productId);
    }

    @PutMapping("/{productId}")
    public ProductResponse update(Authentication auth, @PathVariable Long productId,
            @Valid @RequestBody ProductRequest request) {
        VendorProductController.requireVendor(auth); return service.update(auth.getName(), productId, request);
    }

    @PatchMapping("/{productId}/archive")
    public ProductResponse archive(Authentication auth, @PathVariable Long productId) {
        VendorProductController.requireVendor(auth); return service.archive(auth.getName(), productId);
    }
}
