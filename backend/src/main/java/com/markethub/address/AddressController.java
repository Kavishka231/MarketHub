package com.markethub.address;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/addresses")
public class AddressController {
    private final AddressService service;
    public AddressController(AddressService service) { this.service = service; }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AddressResponse create(Authentication authentication, @Valid @RequestBody AddressRequest request) {
        return service.create(authentication.getName(), request);
    }

    @GetMapping
    public List<AddressResponse> list(Authentication authentication) {
        return service.list(authentication.getName());
    }
}
