package com.markethub.cart;

import java.math.BigDecimal;
import java.util.List;

public record CartResponse(Long cartId, List<CartItemResponse> items, BigDecimal subtotal, int totalItems) {
    public static CartResponse from(Cart cart, List<CartItem> items) {
        List<CartItemResponse> responses = items.stream().map(CartItemResponse::from).toList();
        BigDecimal subtotal = responses.stream().map(CartItemResponse::lineTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        int totalItems = responses.stream().mapToInt(CartItemResponse::quantity).sum();
        return new CartResponse(cart.getId(), responses, subtotal, totalItems);
    }
}
