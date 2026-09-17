package com.markethub.category;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/categories")
public class AdminCategoryController {

    private final CategoryService categoryService;

    public AdminCategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryResponse create(@Valid @RequestBody CategoryRequest request) {
        return categoryService.create(request);
    }

    @PutMapping("/{categoryId}")
    public CategoryResponse update(
            @PathVariable Long categoryId,
            @Valid @RequestBody CategoryRequest request) {
        return categoryService.update(categoryId, request);
    }

    @PatchMapping("/{categoryId}/deactivate")
    public CategoryResponse deactivate(@PathVariable Long categoryId) {
        return categoryService.setActive(categoryId, false);
    }

    @PatchMapping("/{categoryId}/activate")
    public CategoryResponse activate(@PathVariable Long categoryId) {
        return categoryService.setActive(categoryId, true);
    }
}
