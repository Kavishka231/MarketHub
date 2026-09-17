package com.markethub.cart;

public class InsufficientStockException extends RuntimeException {
    public InsufficientStockException() { super("Requested quantity exceeds available stock"); }
}
