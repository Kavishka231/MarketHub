package com.markethub.cart;

public class CartAccessDeniedException extends RuntimeException {
    public CartAccessDeniedException(String message) { super(message); }
}
