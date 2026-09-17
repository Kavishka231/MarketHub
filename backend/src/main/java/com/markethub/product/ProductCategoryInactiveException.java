package com.markethub.product;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class ProductCategoryInactiveException extends RuntimeException {
    public ProductCategoryInactiveException() { super("Category is inactive"); }
}
