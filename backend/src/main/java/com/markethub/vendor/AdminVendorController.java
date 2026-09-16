package com.markethub.vendor;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/vendors")
public class AdminVendorController {

    private final VendorService vendorService;

    public AdminVendorController(VendorService vendorService) {
        this.vendorService = vendorService;
    }

    @GetMapping
    public List<VendorResponse> list(@RequestParam(required = false) VendorStatus status) {
        return vendorService.list(status);
    }

    @PatchMapping("/{vendorId}/approve")
    public VendorResponse approve(@PathVariable Long vendorId) {
        return vendorService.approve(vendorId);
    }

    @PatchMapping("/{vendorId}/reject")
    public VendorResponse reject(@PathVariable Long vendorId) {
        return vendorService.reject(vendorId);
    }
}
