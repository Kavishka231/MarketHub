package com.markethub.category;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> listActive() {
        return categoryRepository.findByActiveTrueOrderByNameAsc().stream()
                .map(CategoryResponse::from)
                .toList();
    }

    @Transactional
    public CategoryResponse create(CategoryRequest request) {
        String name = normalizeName(request.name());
        String slug = CategorySlugGenerator.generate(name);
        ensureUnique(name, slug);

        Category category = new Category(name, slug, normalizeDescription(request.description()));
        try {
            return CategoryResponse.from(categoryRepository.saveAndFlush(category));
        } catch (DataIntegrityViolationException exception) {
            throw new DuplicateCategoryException();
        }
    }

    private void ensureUnique(String name, String slug) {
        if (categoryRepository.existsByNameIgnoreCase(name) || categoryRepository.existsBySlug(slug)) {
            throw new DuplicateCategoryException();
        }
    }

    private String normalizeName(String name) {
        return name.trim().replaceAll("\\s+", " ");
    }

    private String normalizeDescription(String description) {
        if (description == null) {
            return null;
        }
        String normalized = description.trim();
        return normalized.isEmpty() ? null : normalized;
    }
}
