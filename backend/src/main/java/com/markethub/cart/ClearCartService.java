package com.markethub.cart;

import com.markethub.user.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ClearCartService {
    private final CartRepository carts;
    private final CartItemRepository items;
    private final UserRepository users;

    public ClearCartService(CartRepository carts, CartItemRepository items, UserRepository users) {
        this.carts = carts; this.items = items; this.users = users;
    }

    @Transactional
    public void clear(String email) {
        User user = users.findByEmail(email).orElseThrow(() -> new CartAccessDeniedException("Customer account is required"));
        if (user.getRole() != UserRole.CUSTOMER) throw new CartAccessDeniedException("Customer role is required");
        carts.findByCustomerId(user.getId()).ifPresent(cart -> items.deleteByCartId(cart.getId()));
    }
}
