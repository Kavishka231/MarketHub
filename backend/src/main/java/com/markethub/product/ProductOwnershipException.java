package com.markethub.product;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class ProductOwnershipException extends RuntimeException {
    public ProductOwnershipException() { super("Product belongs to another vendor"); }
}
