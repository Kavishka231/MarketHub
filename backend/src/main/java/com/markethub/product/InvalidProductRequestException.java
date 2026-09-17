package com.markethub.product;

public class InvalidProductRequestException extends RuntimeException {
    public InvalidProductRequestException(String message) { super(message); }
}
