package com.markethub.order;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/vendor/orders")
public class VendorOrderController {
    private final VendorOrderService service;
    public VendorOrderController(VendorOrderService service) { this.service=service; }
    @GetMapping
    public VendorOrderPageResponse list(Authentication authentication,
            @RequestParam(required=false) OrderStatus status,
            @RequestParam(defaultValue="0") int page,
            @RequestParam(defaultValue="20") int size) {
        return service.list(authentication.getName(), status, page, size);
    }
}
