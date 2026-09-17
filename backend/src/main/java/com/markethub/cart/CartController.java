package com.markethub.cart;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
public class CartController {
    private final CartService service;
    public CartController(CartService service) { this.service = service; }

    @PostMapping("/items")
    @ResponseStatus(HttpStatus.CREATED)
    public CartItemResponse add(Authentication authentication, @Valid @RequestBody AddCartItemRequest request) {
        return service.add(authentication.getName(), request);
    }
}
