package com.markethub.cart;

public class CartItemOwnershipException extends RuntimeException {
    public CartItemOwnershipException() { super("Cart item belongs to another customer"); }
}
