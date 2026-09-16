package com.markethub.vendor;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/vendors")
public class VendorController {

    private final VendorService vendorService;

    public VendorController(VendorService vendorService) {
        this.vendorService = vendorService;
    }

    @PostMapping("/apply")
    @ResponseStatus(HttpStatus.CREATED)
    public VendorResponse apply(Authentication authentication, @Valid @RequestBody VendorApplicationRequest request) {
        return vendorService.apply(authentication.getName(), request);
    }

    @GetMapping("/me")
    public VendorResponse currentVendor(Authentication authentication) {
        return vendorService.getCurrentVendor(authentication.getName());
    }
}
