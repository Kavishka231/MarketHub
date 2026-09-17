package com.markethub.cart;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
public class ClearCartController {
    private final ClearCartService service;
    public ClearCartController(ClearCartService service) { this.service = service; }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void clear(Authentication authentication) { service.clear(authentication.getName()); }
}
