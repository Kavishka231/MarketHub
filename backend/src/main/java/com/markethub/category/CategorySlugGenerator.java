package com.markethub.category;

import java.text.Normalizer;
import java.util.Locale;

final class CategorySlugGenerator {

    private CategorySlugGenerator() {
    }

    static String generate(String name) {
        String slug = Normalizer.normalize(name, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-|-$", "");
        if (slug.isBlank()) {
            throw new InvalidCategoryNameException();
        }
        return slug;
    }
}
