package com.markethub.category;

public class InvalidCategoryNameException extends RuntimeException {

    public InvalidCategoryNameException() {
        super("Category name must produce a valid slug");
    }
}
