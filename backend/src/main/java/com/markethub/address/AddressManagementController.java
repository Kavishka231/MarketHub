package com.markethub.address;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/addresses")
public class AddressManagementController {
    private final AddressManagementService service;
    public AddressManagementController(AddressManagementService service) { this.service = service; }

    @PutMapping("/{addressId}")
    public AddressResponse update(Authentication authentication, @PathVariable Long addressId,
            @Valid @RequestBody AddressUpdateRequest request) {
        return service.update(authentication.getName(), addressId, request);
    }

    @DeleteMapping("/{addressId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(Authentication authentication, @PathVariable Long addressId) {
        service.delete(authentication.getName(), addressId);
    }

    @PatchMapping("/{addressId}/default")
    public AddressResponse makeDefault(Authentication authentication, @PathVariable Long addressId) {
        return service.makeDefault(authentication.getName(), addressId);
    }
}
