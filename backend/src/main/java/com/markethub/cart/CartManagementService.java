package com.markethub.cart;

import com.markethub.user.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CartManagementService {
    private final CartRepository carts;
    private final CartItemRepository items;
    private final UserRepository users;

    public CartManagementService(CartRepository carts, CartItemRepository items, UserRepository users) {
        this.carts = carts; this.items = items; this.users = users;
    }

    @Transactional
    public CartResponse get(String email) {
        User customer = customer(email);
        Cart cart = carts.findByCustomerId(customer.getId()).orElseGet(() -> carts.saveAndFlush(new Cart(customer)));
        return CartResponse.from(cart, items.findByCartIdOrderByCreatedAtAsc(cart.getId()));
    }

    @Transactional
    public CartItemResponse update(String email, Long itemId, UpdateCartItemRequest request) {
        CartItem item = ownedItem(email, itemId);
        if (request.quantity() > item.getProduct().getStockQuantity()) throw new InsufficientStockException();
        item.setQuantity(request.quantity());
        return CartItemResponse.from(items.saveAndFlush(item));
    }

    @Transactional
    public void remove(String email, Long itemId) { items.delete(ownedItem(email, itemId)); }

    CartItem ownedItem(String email, Long itemId) {
        User customer = customer(email);
        CartItem item = items.findById(itemId).orElseThrow(CartItemNotFoundException::new);
        if (!item.getCart().getCustomer().getId().equals(customer.getId())) throw new CartItemOwnershipException();
        return item;
    }

    User customer(String email) {
        User user = users.findByEmail(email).orElseThrow(() -> new CartAccessDeniedException("Customer account is required"));
        if (user.getRole() != UserRole.CUSTOMER) throw new CartAccessDeniedException("Customer role is required");
        return user;
    }
}
