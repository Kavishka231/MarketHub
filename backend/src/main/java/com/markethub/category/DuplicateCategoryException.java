package com.markethub.category;

public class DuplicateCategoryException extends RuntimeException {

    public DuplicateCategoryException() {
        super("Category name or slug already exists");
    }
}
