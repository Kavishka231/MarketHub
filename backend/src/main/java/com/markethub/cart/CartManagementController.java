package com.markethub.cart;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
public class CartManagementController {
    private final CartManagementService service;
    public CartManagementController(CartManagementService service) { this.service = service; }

    @GetMapping
    public CartResponse get(Authentication authentication) { return service.get(authentication.getName()); }

    @PutMapping("/items/{itemId}")
    public CartItemResponse update(Authentication authentication, @PathVariable Long itemId,
            @Valid @RequestBody UpdateCartItemRequest request) {
        return service.update(authentication.getName(), itemId, request);
    }

    @DeleteMapping("/items/{itemId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remove(Authentication authentication, @PathVariable Long itemId) {
        service.remove(authentication.getName(), itemId);
    }
}
