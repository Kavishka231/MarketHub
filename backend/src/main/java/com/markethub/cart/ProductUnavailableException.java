package com.markethub.cart;

public class ProductUnavailableException extends RuntimeException {
    public ProductUnavailableException() { super("Product is not currently available"); }
}
