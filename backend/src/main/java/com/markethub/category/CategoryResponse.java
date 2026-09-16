package com.markethub.category;

public record CategoryResponse(
        Long id,
        String name,
        String slug,
        String description) {

    static CategoryResponse from(Category category) {
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getSlug(),
                category.getDescription());
    }
}
